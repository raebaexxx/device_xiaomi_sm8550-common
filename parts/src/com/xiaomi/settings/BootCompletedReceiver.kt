/*
 * SPDX-FileCopyrightText: 2023-2025 Paranoid Android
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.Display.HdrCapabilities

import androidx.preference.PreferenceManager

import com.xiaomi.settings.autohbm.AutoHbmActivity
import com.xiaomi.settings.autohbm.AutoHbmFragment
import com.xiaomi.settings.autohbm.AutoHbmTileService
import com.xiaomi.settings.telephony.EsimController
import com.xiaomi.settings.utils.ComponentUtils

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (DEBUG) Log.d(TAG, "Received boot completed intent: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> onBootCompleted(context)
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> onLockedBootCompleted(context)
        }

    try {
        Handler(Looper.getMainLooper()).postDelayed({
        if (DEBUG) Log.d(TAG, "Starting Auto HBM service components")
        AutoHbmFragment.toggleAutoHbmService(context)
        ComponentUtils.toggleComponent(context, AutoHbmActivity::class.java, true)
        ComponentUtils.toggleComponent(context, AutoHbmTileService::class.java, true)
        }, 5000L)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to start AutoHBM components", e)
    }
}

    private fun onBootCompleted(context: Context) {
        // Telephony
        EsimController.getInstance(context).onBootCompleted()
    }

    private fun onLockedBootCompleted(context: Context) {

        // Override HDR types to enable Dolby Vision
        val displayManager = context.getSystemService(DisplayManager::class.java)
        displayManager?.overrideHdrTypes(Display.DEFAULT_DISPLAY, intArrayOf(
            HdrCapabilities.HDR_TYPE_DOLBY_VISION,
            HdrCapabilities.HDR_TYPE_HDR10,
            HdrCapabilities.HDR_TYPE_HLG,
            HdrCapabilities.HDR_TYPE_HDR10_PLUS
        ))
    }
}