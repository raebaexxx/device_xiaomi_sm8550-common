/*
 *  Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *  Not a Contribution.
 *
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2018 The LineageOS Project
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

// Max ACL connections (for multiple BT devices)
#define MAX_ACL_CONNECTIONS   16
#define MAX_L2CAP_CHANNELS    32

// BLE & GATT Support
#define BLE_VND_INCLUDED      TRUE
#define GATT_INCLUDED         TRUE
#define GATT_MAX_PHY_CHANNEL  10
#define GATT_MAX_SR_PROFILES  10
#define GATT_MAX_CL_PROFILES  10

// Security & Pairing
#define SMP_INCLUDED          TRUE
#define BTA_DM_IOCAP          IO_CAP_KBDISP  // Phone can display & input PIN

// Profiles for Smartwatches
#define HID_HOST_INCLUDED     TRUE
#define HID_DEV_INCLUDED      TRUE
#define BTA_JV_INCLUDED       TRUE  // Enables SPP, etc.

// Audio (if watch supports calls/music)
#define BTA_AV_SINK_INCLUDED  TRUE
#define AVDT_NUM_SEPS         35

#endif
