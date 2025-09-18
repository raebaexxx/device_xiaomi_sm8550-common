/*
 *  Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *  Not a Contribution.
 *
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2018 The LineageOS Project
 * Copyright (C) 2025 YourName (для Evolution X Android 16)
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

#ifndef _BDROID_BUILDCFG_H
#define _BDROID_BUILDCFG_H

// Max number of ACL connections (headsets, watches, etc.)
#define MAX_ACL_CONNECTIONS   16
#define MAX_L2CAP_CHANNELS    32

// Enable BLE Vendor extensions
#define BLE_VND_INCLUDED      TRUE

// GATT settings — КРИТИЧНО ДЛЯ ЧАСОВ
#define BTA_GATT_INCLUDED     TRUE   // Включает GATT-клиент
#define GATT_MAX_PHY_CHANNEL  10

// Battery Service — чтобы часы передавали заряд
#define BTA_BAS_INCLUDED      TRUE

// Human Interface Device — для некоторых часов/браслетов
#define BTA_HH_INCLUDED       TRUE

// Message Access Profile — для уведомлений
#define BTA_MCE_INCLUDED      TRUE

// Audio/AV settings
#define AVDT_NUM_SEPS         35

// Optional: Enable debug logs for GATT (временно, для диагностики)
// #define BT_DEBUGGABLE TRUE

#endif
