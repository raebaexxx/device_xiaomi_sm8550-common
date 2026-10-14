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

    // HyperCharge
    public static final String KEY_HYPERCHARGE_STATUS = "hypercharge_status";
    public static final String KEY_HYPERCHARGE_LIMIT = "hypercharge_limit";

    /**
     * Values written to constant_charge_current, in microamps.
     *
     * <p>These are battery side current, not watts. Upstream ships presets named
     * after wattages (120W, 90W, 67W) which are calibrated for devices with much
     * larger cells; on fuxi the coprocessor refuses anything above 6.55 A with
     * -EINVAL, so those presets cannot be reproduced. The numbers below are what
     * the hardware here actually accepts, measured on device.
     *
     * <p>{@link #CHARGE_LIMIT_MAX} writes nothing at all. With the thermal daemon
     * no longer allowed to write, leaving the node alone lets the coprocessor
     * apply its own ceiling, which is the fastest this hardware goes.
     */
    public static final String CHARGE_LIMIT_MAX = "";
    public static final String CHARGE_LIMIT_6550 = "6550000";
    public static final String CHARGE_LIMIT_6000 = "6000000";
    public static final String CHARGE_LIMIT_5500 = "5500000";
    public static final String CHARGE_LIMIT_5000 = "5000000";
    public static final String CHARGE_LIMIT_4500 = "4500000";

    public static final String NODE_BATTERY = "/sys/class/power_supply/battery/";
    public static final String NODE_QCOM_BATTERY = "/sys/class/qcom-battery/";

    // XM channel attributes. These exist only with the full Xiaomi charger
    // driver; the reduced Lineage one does not expose them at all.
    public static final String NODE_FASTCHG_MODE = NODE_QCOM_BATTERY + "fastchg_mode";
    public static final String NODE_FCC = "constant_charge_current";
    public static final String NODE_CHG_CTRL_LIM = "charge_control_limit";
    public static final String NODE_CHG_CTRL_LIM_MAX = "charge_control_limit_max";
    public static final String NODE_REFERANCE_POWER = "referance_power";
    public static final String NODE_SET_LEARN_POWER = "set_learn_power";
    public static final String NODE_SET_LEARN_POWER_B = "set_learn_power_b";
    public static final String NODE_FG2_REFERANCE_POWER = "fg2_referance_power";
    public static final String NODE_FG2_SET_LEARN_POWER = "fg2_set_learn_power";
    public static final String NODE_FG2_SET_LEARN_POWER_B = "fg2_set_learn_power_b";
    public static final String NODE_FG2_CONSTANT_POWER = "fg2_constant_power";
    public static final String NODE_CONSTANT_POWER = "constant_power";
    public static final String NODE_CP_OVP_MODE = "cp_ovp_mode";
    public static final String NODE_DAM_OVPGATE = "dam_ovpgate";
    public static final String NODE_BATTMONI_ISC = "battmoni_isc";

    /**
     * A current in microamps, not a flag. Writing 1 here clamps charging to
     * 1 uA and the phone sits at about 1 W. Only ever write 0 or a value
     * worked out as an actual current.
     */
    public static final String NODE_MTBF_CURRENT = "mtbf_current";
    public static final String NODE_FG1_FCC = "fg1_fcc";
    public static final String NODE_POWER_MAX = "power_max";
    public static final String NODE_CHARGING_ENABLED = "charging_enabled";

    /**
     * The gate that actually moves the charge current.
     *
     * <p>Measured on device, thermal level held constant on all three:
     *
     * <pre>
     *   thermal_remove=0 -&gt; fcc 5280000  (5.28 A)
     *   thermal_remove=1 -&gt; fcc 6550000  (6.55 A)
     * </pre>
     *
     * <p>Across levels 0, 1, 2 and 5 {@link #NODE_CHG_CTRL_LIM} changes nothing,
     * so that node is not the lever it looks like.
     *
     * <p>pd_verifed was the previous choice here and is wrong for this device.
     * It is the coprocessor's PD authentication flag, and setting it makes the
     * firmware drop MiPPS, Xiaomi's own encrypted protocol:
     *
     * <pre>
     *   pd_verifed=0 -&gt; adapter_svid 2717, real_type PD_PPS  MiPPS on
     *   pd_verifed=1 -&gt; adapter_svid 0000, real_type PD     MiPPS off
     * </pre>
     *
     * <p>It happens to reach the same 6.55 A ceiling, but by turning off the
     * protocol that 67 W charging actually runs on. Upstream's FreePPS module
     * does exactly this and warns that PPS and MiPPS conflict and cannot both
     * be enabled; it targets phones with Surge P1/P2 charge ICs, which fuxi
     * does not have. fuxi uses a bq2597x.
     *
     * <p>{@code authentic}, {@code slave_authentic} and {@code request_vdm_cmd}
     * were each tested on their own and none of them move fcc.
     *
     * <p>What this removes exactly is not known, the coprocessor is closed. It
     * is a thermal gate, so it is opt in and not on by default.
     */
    public static final String NODE_THERMAL_REMOVE = "/sys/class/qcom-battery/thermal_remove";

    // Attributes that are logged before and after every change, to see what
    // the coprocessor actually does rather than what it is asked to do.
    public static final String[] HYPERCHARGE_WATCH = {
            NODE_CHG_CTRL_LIM_MAX, NODE_FCC, NODE_FASTCHG_MODE, NODE_POWER_MAX,
            NODE_FG1_FCC, NODE_REFERANCE_POWER, NODE_SET_LEARN_POWER,
            NODE_SET_LEARN_POWER_B, NODE_FG2_REFERANCE_POWER,
            NODE_FG2_SET_LEARN_POWER, NODE_FG2_SET_LEARN_POWER_B,
            NODE_FG2_CONSTANT_POWER, NODE_CONSTANT_POWER, NODE_CP_OVP_MODE,
            NODE_DAM_OVPGATE, NODE_BATTMONI_ISC, NODE_MTBF_CURRENT,
            NODE_CHARGING_ENABLED, NODE_THERMAL_REMOVE,
    };
}
