/*
 * Copyright (C) 2022-2026 The Xiaomi Settings Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.settings.utils;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.settings.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtils {

    private static final String TAG = "XiaomiSettingsUtils";

    // --- Component Utils ---
    public static void toggleComponent(Context context, Class<?> componentClass, boolean enable) {
        if (context == null || componentClass == null) return;
        ComponentName componentName = new ComponentName(context, componentClass);
        PackageManager packageManager = context.getPackageManager();
        int newState = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if (packageManager.getComponentEnabledSetting(componentName) != newState) {
            packageManager.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
        }
    }

    // --- File Utils ---
    public static String readLine(String fileName) {
        if (fileName == null) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName), 512)) {
            return reader.readLine();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + fileName, e);
            return null;
        }
    }

    public static String getFileValue(String fileName, String defaultValue) {
        String line = readLine(fileName);
        return (line != null && !line.trim().isEmpty()) ? line.trim() : defaultValue;
    }

    public static int readLineInt(String fileName) {
        String line = readLine(fileName);
        if (line == null) return 0;
        try {
            return Integer.parseInt(line.replace("0x", "").trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid integer in file: " + fileName, e);
            return 0;
        }
    }

    public static boolean writeLine(String fileName, Object value) {
        if (fileName == null || value == null) return false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.valueOf(value));
            writer.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file: " + fileName, e);
            return false;
        }
    }

    public static boolean rename(String srcPath, String dstPath) {
        if (srcPath == null || dstPath == null) return false;
        try {
            return new File(srcPath).renameTo(new File(dstPath));
        } catch (SecurityException e) {
            Log.w(TAG, "Rename failed: " + srcPath, e);
            return false;
        }
    }

    public static boolean fileExists(String fileName) {
        return fileName != null && new File(fileName).exists();
    }

    // --- Tile Utils ---
    public static void requestAddTileService(Context context, Class<?> tileServiceClass, int labelResId, int iconResId) {
        if (context == null || tileServiceClass == null) return;
        StatusBarManager sbm = context.getSystemService(StatusBarManager.class);
        if (sbm == null) return;

        ComponentName componentName = new ComponentName(context, tileServiceClass);
        String label = context.getString(labelResId);
        Icon icon = Icon.createWithResource(context, iconResId);

        sbm.requestAddTileService(componentName, label, icon, context.getMainExecutor(),
                result -> handleTileResult(context, result));
    }

    private static void handleTileResult(Context context, int result) {
        int messageResId;
        switch (result) {
            case StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED:
                messageResId = R.string.tile_added;
                break;
            case StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED:
                messageResId = R.string.tile_already_added;
                break;
            default:
                messageResId = R.string.tile_not_added;
                break;
        }
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }
}
