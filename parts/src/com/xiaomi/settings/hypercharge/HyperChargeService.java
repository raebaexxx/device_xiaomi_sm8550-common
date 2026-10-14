/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.settings.hypercharge;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.xiaomi.settings.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Applies a charge power profile over the Xiaomi XM channel.
 *
 * <p>The XM attributes this talks to exist only in the full
 * qti_battery_charger driver. With the reduced driver shipped by the
 * Lineage base they are absent, and nothing here has any effect.
 *
 * <p>Targets are held in shared preferences under {@code hc_target_<attr>}
 * so they can be changed at runtime:
 *
 * <pre>
 * su -c 'am broadcast -a com.xiaomi.settings.HYPERCHARGE_APPLY'
 * </pre>
 *
 * or directly with the settings UI. That matters because the useful
 * values are not known ahead of time: an empty target is skipped, so a
 * profile can be built up one attribute at a time without rebuilding.
 */
public class HyperChargeService extends Service {

    private static final String TAG = "HyperCharge";

    private static final String PREF_TARGET_PREFIX = "hc_target_";

    /**
     * mi_thermald is no longer allowed to write these nodes at all, so this is
     * not about fighting that daemon. batterysecret still holds write on
     * vendor_sysfs_battery_supply and keeps walking the level up on its own
     * schedule, roughly every 80 s. Measured on device with a 30 s interval,
     * the current collapsed from 30.7 W to 10.2 W between reassertions:
     *
     *   t=90s  level 0   fcc 7870000  30.6 W
     *   t=105s level 12  fcc 2100000  16.5 W
     *   t=150s level 0   fcc 7870000   9.5 W
     *   t=165s level 8   fcc 4400000  16.8 W
     *
     * Five seconds matches what upstream uses and leaves the rival writer
     * almost no window. The loop reads before it writes, so the cost when
     * nothing has changed is two sysfs reads.
     */
    private static final long REAPPLY_INTERVAL_MS = 5_000L;

    public static final String ACTION_APPLY = "com.xiaomi.settings.HYPERCHARGE_APPLY";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "driver present: " + hasFullDriver());
        dumpState("service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || ACTION_APPLY.equals(intent.getAction())) {
            apply();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * The reduced driver has no XM channel at all. Detect it once so the
     * logs say so plainly instead of showing a field of empty reads.
     */
    public static boolean hasFullDriver() {
        // NODE_FASTCHG_MODE is already absolute. Prefixing NODE_BATTERY produced
        // /sys/class/power_supply/battery//sys/class/qcom-battery/fastchg_mode,
        // which never exists, so this used to report the full driver as absent.
        return new File(Constants.NODE_FASTCHG_MODE).exists();
    }

    private boolean isEnabled() {
        return getSharedPreferences("com.xiaomi.settings", MODE_PRIVATE)
                .getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false);
    }

    private void apply() {
        boolean enabled = isEnabled();

        Log.i(TAG, "apply: enabled=" + enabled);

        if (!hasFullDriver()) {
            Log.w(TAG, "XM channel absent, nothing to do. The full Xiaomi charger "
                    + "driver is required; the reduced one has no fast charging control.");
            return;
        }

        // fastchg_mode is read only, it reports whether the coprocessor is in
        // fast charge mode. It is not a control, and there is nothing to skip
        // on: level 0 raises the ceiling on a foreign brick too.
        String fastchg = read(Constants.NODE_FASTCHG_MODE);
        Log.i(TAG, "fastchg_mode = " + fastchg);

        // The lever. thermal_remove reaches the coprocessor as
        // XM_PROP_THERMAL_REMOVE and lifts the charge current ceiling:
        //
        //   thermal_remove=0 -> fcc 5280000
        //   thermal_remove=1 -> fcc 6550000
        //
        // It leaves the PD protocol alone. pd_verifed reached the same
        // ceiling by dropping MiPPS, which is the protocol 67 W charging
        // actually runs on, so it is not used here. See Constants.
        //
        // Written on both edges: leaving it at 1 after the switch is turned
        // off would keep the thermal gate open with nothing left to turn it
        // back off.
        if (write(Constants.NODE_THERMAL_REMOVE, enabled ? "1" : "0")) {
            Log.i(TAG, "thermal_remove set to " + (enabled ? "1" : "0")
                    + (enabled ? ", charge ceiling raised" : ", restored"));
        }

        // charge_control_limit is not a current. It is an index into the thermal
        // level table, and that table lives in the coprocessor firmware.
        // batterysecret still walks this level up on its own, so pinning it
        // costs one write and keeps that out of the way. It is not what raises
        // the ceiling, that is thermal_remove above.
        if (enabled) {
            if (write(Constants.NODE_CHG_CTRL_LIM, "0")) {
                Log.i(TAG, "charge_control_limit set to level 0");
            }
        }

        if (enabled && "1".equals(read(Constants.NODE_THERMAL_REMOVE))) {
            String target = getTargetLimit();
            if (!target.isEmpty()) {
                write(Constants.NODE_FCC, target);
            } else {
                Log.i(TAG, "no user limit selected, hardware ceiling applies");
            }
        }

        int written = 0;
        for (String attr : Constants.HYPERCHARGE_WATCH) {
            String target = getSharedPreferences("com.xiaomi.settings", MODE_PRIVATE)
                    .getString(PREF_TARGET_PREFIX + attr, null);
            if (target == null || target.isEmpty()) {
                continue;
            }
            if (write(attr, target)) {
                written++;
            }
        }

        Log.i(TAG, "apply: wrote " + written + " attributes");
        dumpState("after apply");
        scheduleReapply(enabled);
    }

    /**
     * batterysecret still walks charge_control_limit up on its own schedule,
     * roughly every 80 seconds, raising the level as the phone warms up.
     * Setting it once therefore only holds for about a minute. While
     * HyperCharge is on the level is pushed back down on a shorter interval
     * than that; when it is off the loop stops and nothing is written.
     */
    private void scheduleReapply(boolean enabled) {
        mHandler.removeCallbacks(mReapply);
        if (!enabled) {
            Log.i(TAG, "reapply stopped, thermal daemon left in charge");
            return;
        }
        mHandler.postDelayed(mReapply, REAPPLY_INTERVAL_MS);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable mReapply = new Runnable() {
        @Override
        public void run() {
            if (!isEnabled()) {
                Log.i(TAG, "reapply stopped, HyperCharge switched off");
                return;
            }

            // The gate. thermal_remove=1 raises the coprocessor's charge
            // current ceiling from 5.28 A to 6.55 A. It does not touch the PD
            // protocol, which is what setting pd_verifed did.
            //
            // Only write when it actually reads 0. Writing unconditionally on
            // every tick means tens of thousands of no-op writes a day and a
            // log line for each, which is what the read before the write here
            // avoids.
            if (!"1".equals(read(Constants.NODE_THERMAL_REMOVE))) {
                if (write(Constants.NODE_THERMAL_REMOVE, "1")) {
                    Log.i(TAG, "thermal_remove set to 1, charge ceiling raised");
                }
            }

            // Keep the level pinned as well. It is not what lifts the current,
            // but it costs one write and keeps batterysecret from walking it.
            write(Constants.NODE_CHG_CTRL_LIM, "0");

            // Then pin the current, but only if something moved it off the
            // target. Writing unconditionally every tick would fight the
            // coprocessor for no reason, and read first so the log says what
            // actually changed.
            String target = getTargetLimit();
            if (!target.isEmpty()) {
                String current = read(Constants.NODE_FCC);
                if (!target.equals(current)) {
                    Log.i(TAG, "holding fcc at " + target + ", was " + current);
                    write(Constants.NODE_FCC, target);
                }
            }

            mHandler.postDelayed(this, REAPPLY_INTERVAL_MS);
        }
    };

    /**
     * The current ceiling the user picked, or an empty string meaning leave the
     * node alone. Leaving it alone is the fastest case: the thermal daemon can
     * no longer write it either, so the coprocessor applies its own ceiling.
     */
    private String getTargetLimit() {
        if (!isEnabled()) {
            return Constants.CHARGE_LIMIT_MAX;
        }
        return getSharedPreferences("com.xiaomi.settings", MODE_PRIVATE)
                .getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_MAX);
    }

    private boolean write(String attr, String value) {
        String before = read(attr);
        File f = new File(path(attr));
        try (FileWriter w = new FileWriter(f)) {
            w.write(value);
        } catch (IOException e) {
            Log.w(TAG, attr + " write failed: " + e.getMessage());
            return false;
        }
        sleep(150);
        String after = read(attr);
        if (before == null || before.equals(after)) {
            Log.w(TAG, attr + " unchanged " + before + " -> " + after
                    + ", write was " + value);
        } else {
            Log.i(TAG, attr + " " + before + " -> " + after + " (asked " + value + ")");
        }
        return true;
    }

    /**
     * Logs the PD contract, the battery side and every XM attribute, so a
     * change can be judged on what the coprocessor reports afterwards
     * rather than on what was requested of it.
     */
    public static void dumpState(String why) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== state (").append(why).append(") ===");

        sb.append(" usb=").append(readUsb("voltage_now"))
          .append('/').append(readUsb("current_max"));
        sb.append(" soc=").append(read("capacity"))
          .append(" vbat=").append(read("voltage_now"))
          .append(" ibat=").append(read("current_now"));

        for (String attr : Constants.HYPERCHARGE_WATCH) {
            String v = read(attr);
            if (v != null) {
                sb.append('\n').append("  ").append(attr).append(" = ").append(v);
            }
        }
        Log.i(TAG, sb.toString());
    }

    /** Attributes live under power_supply, except fastchg_mode which is under qcom-battery. */
    private static String path(String attr) {
        return attr.startsWith("/") ? attr : Constants.NODE_BATTERY + attr;
    }

    public static String read(String attr) {
        try (BufferedReader r = new BufferedReader(new FileReader(path(attr)))) {
            String line = r.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            return null;
        }
    }

    private static String readUsb(String attr) {
        try (BufferedReader r = new BufferedReader(
                new FileReader("/sys/class/power_supply/usb/" + attr))) {
            String line = r.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}