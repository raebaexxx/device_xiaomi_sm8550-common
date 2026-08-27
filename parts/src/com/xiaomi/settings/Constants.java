/*
 * Copyright (C) 2024 The LineageOS Project
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

package com.xiaomi.settings;

public class Constants {

    // AutoHbm
    public static final String KEY_AUTO_HBM = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_AUTO_HBM_ENABLE_TIME = "auto_hbm_enable_time";
    public static final String KEY_AUTO_HBM_DISABLE_TIME = "auto_hbm_disable_time";
    public static final String KEY_CURRENT_LUX_LEVEL = "current_lux_level";
    public static final String NODE_BRIGHTNESS = "/sys/class/backlight/panel0-backlight/brightness";

    // HyperCharge (67W wired fast charging on fuxi / SM8550)
    public static final String KEY_HYPERCHARGE_STATUS = "hypercharge_status";
    public static final String KEY_HYPERCHARGE_LIMIT = "hypercharge_limit";

    // /sys/class/qcom-battery/fastchg_mode is read-only status (0=std, 1=hypercharge)
    public static final String NODE_FASTCHG_MODE = "/sys/class/qcom-battery/fastchg_mode";
    // constant_charge_current (µA) is the writable FCC cap node
    public static final String NODE_CONSTANT_CHARGE_CURRENT = "/sys/class/power_supply/battery/constant_charge_current";

    // µA values written to constant_charge_current (22000000 = no cap → factory max)
    public static final String CHARGE_LIMIT_120W = "22000000";
    public static final String CHARGE_LIMIT_90W = "16500000";
    public static final String CHARGE_LIMIT_67W = "12300000";
    public static final String CHARGE_LIMIT_50W = "9200000";
    public static final String CHARGE_LIMIT_33W = "6000000";
    public static final String CHARGE_LIMIT_30W = "5500000";
}
