/*
 * SPDX-FileCopyrightText: 2023-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.Display.HdrCapabilities
import com.xiaomi.settings.autohbm.AutoHbmActivity
import com.xiaomi.settings.autohbm.AutoHbmFragment
import com.xiaomi.settings.autohbm.AutoHbmTileService
import com.xiaomi.settings.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
        private const val AUTO_HBM_DELAY_MS = 5000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (DEBUG) Log.d(TAG, "Received boot intent: $action")
        when (action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> onLockedBootCompleted(context)
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted(context)
        }
    }

    private fun onBootCompleted(context: Context) {
        val pendingResult = goAsync() 
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (DEBUG) Log.d(TAG, "Starting Auto HBM service components")
                AutoHbmFragment.toggleAutoHbmService(context)
                FileUtils.toggleComponent(context, AutoHbmActivity::class.java, true)
                FileUtils.toggleComponent(context, AutoHbmTileService::class.java, true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AutoHBM components", e)
            } finally {
                pendingResult.finish()
            }
        }, AUTO_HBM_DELAY_MS)
    }

    private fun onLockedBootCompleted(context: Context) {
        try {
            // Override HDR types to enable Dolby Vision
            val displayManager = context.getSystemService(DisplayManager::class.java)
            displayManager?.overrideHdrTypes(
                Display.DEFAULT_DISPLAY, intArrayOf(
                    HdrCapabilities.HDR_TYPE_DOLBY_VISION,
                    HdrCapabilities.HDR_TYPE_HDR10,
                    HdrCapabilities.HDR_TYPE_HLG,
                    HdrCapabilities.HDR_TYPE_HDR10_PLUS
                )
            )
            if (DEBUG) Log.d(TAG, "Successfully overridden HDR types")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to override HDR types", e)
        }
    }
}