/*
 * Copyright (C) 2023 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.Display.HdrCapabilities;

import com.xiaomi.settings.touch.TouchOrientationService;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "XiaomiParts";
    private static final boolean DEBUG = true;
    private static final int GESTURE_INIT_DELAY_MS = 5000; // 5 seconds

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }
        if (DEBUG)
            Log.d(TAG, "Received boot completed intent");
        
        try {
            if (DEBUG) Log.d(TAG, "Starting TouchOrientationService");
            // Touchscreen
            context.startServiceAsUser(new Intent(context, TouchOrientationService.class),
                    UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start TouchOrientationService", e);
        }

        try {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (DEBUG) Log.d(TAG, "Initializing GestureUtils after delay");
                com.xiaomi.settings.utils.GestureUtils.init(context);
            }, GESTURE_INIT_DELAY_MS);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize GestureUtils", e);
        }

        // Override HDR types to enable Dolby Vision
        final DisplayManager displayManager = context.getSystemService(DisplayManager.class);
        displayManager.overrideHdrTypes(Display.DEFAULT_DISPLAY,
                new int[] {HdrCapabilities.HDR_TYPE_DOLBY_VISION, HdrCapabilities.HDR_TYPE_HDR10,
                        HdrCapabilities.HDR_TYPE_HLG, HdrCapabilities.HDR_TYPE_HDR10_PLUS});
    }
}