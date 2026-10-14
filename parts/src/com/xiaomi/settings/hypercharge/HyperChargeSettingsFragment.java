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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.android.settingslib.widget.MainSwitchPreference;

import com.xiaomi.settings.Constants;
import com.xiaomi.settings.R;

public class HyperChargeSettingsFragment extends PreferenceFragment {

    private static final String DUMP_KEY = "hypercharge_dump_state";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.hypercharge_settings);

        MainSwitchPreference mainSwitch = findPreference(Constants.KEY_HYPERCHARGE_STATUS);
        if (mainSwitch != null) {
            mainSwitch.setOnPreferenceChangeListener((pref, newValue) -> {
                pref.getSharedPreferences().edit()
                        .putBoolean(Constants.KEY_HYPERCHARGE_STATUS, (Boolean) newValue)
                        .apply();
                apply();
                return true;
            });
        }

        Preference dump = findPreference(DUMP_KEY);
        if (dump != null) {
            dump.setOnPreferenceClickListener(pref -> {
                HyperChargeService.dumpState("manual dump");
                return true;
            });
        }

        // Applying only on the main switch would leave a changed current ceiling
        // sitting in preferences until the next toggle.
        ListPreference limit = findPreference(Constants.KEY_HYPERCHARGE_LIMIT);
        if (limit != null) {
            limit.setOnPreferenceChangeListener((pref, newValue) -> {
                // Returning true makes the framework persist the value, and it
                // does that after this listener returns. Applying inside the
                // listener would read the old value, so wait a turn.
                mHandler.post(this::apply);
                return true;
            });
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onResume() {
        super.onResume();
        HyperChargeService.dumpState("settings resumed");
    }

    private void apply() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Intent i = new Intent(context, HyperChargeService.class)
                .setAction(HyperChargeService.ACTION_APPLY);
        context.startService(i);
    }
}