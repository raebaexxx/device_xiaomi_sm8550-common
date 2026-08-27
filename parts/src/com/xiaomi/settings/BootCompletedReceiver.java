/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.Display.HdrCapabilities;

import androidx.preference.PreferenceManager;

import com.xiaomi.settings.hypercharge.HyperChargeService;
import com.xiaomi.settings.thermal.ThermalService;
import com.xiaomi.settings.thermal.ThermalUtils;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "XiaomiParts";
    private static final boolean DEBUG = true;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }
        if (DEBUG)
            Log.d(TAG, "Received boot completed intent");

        // Start ThermalService
        try {
            ThermalUtils thermalUtils = ThermalUtils.getInstance(context);
            if (thermalUtils.isEnabled()) {
                Intent thermalServiceIntent = new Intent(context, ThermalService.class);
                context.startService(thermalServiceIntent);
                if (DEBUG) Log.d(TAG, "Started ThermalService");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start ThermalService", e);
        }

        // Start HyperChargeService if the user disabled the default (full speed) mode
        // or turned off the toggle — service enforces the chosen current cap.
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isHyperChargeEnabled = prefs.getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false);
            String currentLimit = prefs.getString(Constants.KEY_HYPERCHARGE_LIMIT, Constants.CHARGE_LIMIT_120W);

            if (!isHyperChargeEnabled || !Constants.CHARGE_LIMIT_120W.equals(currentLimit)) {
                Intent hyperChargeServiceIntent = new Intent(context, HyperChargeService.class);
                context.startService(hyperChargeServiceIntent);
                if (DEBUG) Log.d(TAG, "Started HyperChargeService");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start HyperChargeService", e);
        }

        // Override HDR types to enable Dolby Vision
        final DisplayManager displayManager = context.getSystemService(DisplayManager.class);
        displayManager.overrideHdrTypes(Display.DEFAULT_DISPLAY,
                new int[] {HdrCapabilities.HDR_TYPE_DOLBY_VISION, HdrCapabilities.HDR_TYPE_HDR10,
                        HdrCapabilities.HDR_TYPE_HLG, HdrCapabilities.HDR_TYPE_HDR10_PLUS});
    }
}