/*
 * Copyright (C) 2019 The MoKee Open Source Project
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

package com.android.bluetooth.bthelper.models;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import java.util.Arrays;
import java.util.List;

import com.android.bluetooth.bthelper.AirPodsUtils;
import com.android.bluetooth.bthelper.R;

public class AirPodsProGen2 {

    private static final byte[] LOW_BATTERY_THRESHOLD = (""+20).getBytes();

    private static final byte[] MANUFACTURER_NAME = "Apple Inc.".getBytes();
    private static final byte[] MODEL_NAME = "AirPods Pro".getBytes();

    private static final int FLAG_REVERSED = 1 << 7;

    private static final int MASK_CHARGING_LEFT = 1 << 5;
    private static final int MASK_CHARGING_RIGHT = 1 << 4;
    private static final int MASK_CHARGING_CASE = 1 << 6;

    private static final int MASK_USING_LEFT = 1 << 3;
    private static final int MASK_USING_RIGHT = 1 << 1;

    private static boolean isModelSet = false;
    private static boolean isModelIconSet = false;
    private static boolean isModelLowBatteryThresholdSet = false;

    private static byte[] LeftBatteryOld;
    private static byte[] RightBatteryOld;
    private static byte[] CaseBatteryOld;
    private static byte[] batteryNew;

    private static int batteryTmp = 0;

    private static int batteryLeft, batteryRight, batteryCase, batteryUnified;
    private static boolean rightLeft;
    private static boolean chargingLeft, chargingRight, chargingCase, chargingMain;
    private static boolean chargingLeftOld, chargingRightOld, chargingCaseOld, chargingMainOld;
    private static boolean usingLeft, usingRight;
    private static boolean usingLeftOld, usingRightOld;

    private static final byte[] TRUE = "true".getBytes();
    private static final byte[] FALSE = "false".getBytes();
    
    private static byte[] MODEL_ICON_URI;
    private static byte[] MODEL_ICON_URI_LEFT;
    private static byte[] MODEL_ICON_URI_RIGHT;
    private static byte[] MODEL_ICON_URI_CASE;

    public static void setModelData(byte[] data) {
        final int flags = data[5];
        final int battery = data[6];
        final int charging = data[7];

        rightLeft = ((flags & FLAG_REVERSED) != 0);
    
        if (rightLeft == false) {
            batteryLeft = (battery >> 4) & 0xf;
            batteryRight = battery & 0xf;
            chargingLeft = (charging & MASK_CHARGING_LEFT) != 0;
            chargingRight = (charging & MASK_CHARGING_RIGHT) != 0;
        } else {
            batteryLeft = battery & 0xf;
            batteryRight = (battery >> 4) & 0xf;
            chargingLeft = (charging & MASK_CHARGING_RIGHT) != 0;
            chargingRight = (charging & MASK_CHARGING_LEFT) != 0;
        }

        batteryCase = charging & 0xf;
        chargingCase = (charging & MASK_CHARGING_CASE) != 0;

        usingLeft = (flags & (rightLeft == true ? MASK_USING_LEFT : MASK_USING_RIGHT)) != 0;
        usingRight = (flags & (rightLeft == true ? MASK_USING_RIGHT : MASK_USING_LEFT)) != 0;

        if (chargingLeft == true
            && chargingRight == true) {
            chargingMain = true;
        } else {
            chargingMain = false; 
        }

        // Log.d(TAG, String.format("%s\t%s\tL: %s (%s)\tR: %s (%s)\tCASE: %s (%s)",
        //         address, rightLeft,
        //         batteryLeft == 0xf ? "-" : batteryLeft, usingLeft ? "USE" : chargingLeft ? "CHG" : "---",
        //         batteryRight == 0xf ? "-" : batteryRight, usingRight ? "USE" : chargingRight ? "CHG" : "---",
        //         batteryCase == 0xf ? "-" : batteryCase, chargingCase ? "CHG" : "---"));

        batteryUnified = Math.min(batteryLeft, batteryRight);
    }

    public static boolean isModelStateChanged() {
        boolean isChanged = false;
        if ((chargingLeft != chargingLeftOld)
            || (chargingRight != chargingRightOld)
            || (chargingCase != chargingCaseOld)
            || (chargingMain != chargingMainOld)
            || (usingLeft != usingLeftOld)
            || (usingRight != usingRightOld)) {
            isChanged = true;
        }
        return isChanged;
    }

    public static Object[] getModelArguments() {
        final Object[] arguments = new Object[] {
            1, // Number of key(IndicatorType)/value pairs
            BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_IPHONEACCEV_BATTERY_LEVEL, // IndicatorType: Battery Level
            AirPodsUtils.setBatteryLevel(batteryUnified, true), // Battery Level
        };

        return arguments;
    }

    public static void setModelMetaData(Context context, BluetoothDevice mCurrentDevice) {
        if (isModelSet == false) {
            boolean isModelManufacturerSet = false;
            boolean isModelNameSet = false;
            boolean isModelTypeSet = false;
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_MANUFACTURER_NAME) == null) {
                isModelManufacturerSet = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MANUFACTURER_NAME,
                                                            MANUFACTURER_NAME);
            } else {
                isModelManufacturerSet = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_MODEL_NAME) == null) {
                isModelNameSet = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MODEL_NAME,
                                                            MODEL_NAME);
            } else {
                isModelNameSet = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_DEVICE_TYPE) == null) {
                isModelTypeSet = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_DEVICE_TYPE,
                                                            BluetoothDevice.DEVICE_TYPE_UNTETHERED_HEADSET.getBytes());
            } else {
                isModelTypeSet = true;
            }
            isModelSet = isModelManufacturerSet == true
                         && isModelNameSet == true
                         && isModelTypeSet == true;
        }

        if (isModelIconSet == false) {
            String[] packageNames = new String[] {
                "android", "android.bluetooth",
                "com.android.settings", "com.android.bluetooth",
                "com.android.settingslib", "com.android.bluetooth.bthelper",
                "com.android.systemui"
            };
            MODEL_ICON_URI = 
                    AirPodsUtils.resToUri(context, R.drawable.AirPods_Pro, packageNames).toString().getBytes();
            MODEL_ICON_URI_LEFT = 
                    AirPodsUtils.resToUri(context, R.drawable.AirPods_Pro_Left, packageNames).toString().getBytes();
            MODEL_ICON_URI_RIGHT = 
                    AirPodsUtils.resToUri(context, R.drawable.AirPods_Pro_Right, packageNames).toString().getBytes();
            MODEL_ICON_URI_CASE = 
                    AirPodsUtils.resToUri(context, R.drawable.AirPods_Pro_Case, packageNames).toString().getBytes();

            boolean isMainIconset = false;
            boolean isLeftIconset = false;
            boolean isRightIconset = false;
            boolean isCaseIconset = false;
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_MAIN_ICON) == null) {
                isMainIconset = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MAIN_ICON,
                                                                MODEL_ICON_URI);
            } else {
                isMainIconset = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_ICON) == null) {
                isLeftIconset = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_ICON,
                                                                MODEL_ICON_URI_LEFT);
            } else {
                isLeftIconset = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_ICON) == null) {
                isRightIconset = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_ICON,
                                                                MODEL_ICON_URI_RIGHT);
            } else {
                isRightIconset = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_ICON) == null) {
                isCaseIconset = mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_ICON,
                                                                MODEL_ICON_URI_CASE);
            } else {
                isCaseIconset = true;
            }
            isModelIconSet = isMainIconset == true
                             && isLeftIconset == true
                             && isRightIconset == true
                             && isCaseIconset == true;
        }

        if (isModelLowBatteryThresholdSet == false) {
            boolean isMainLowBatteryThresholdSet = false;
            boolean isLeftLowBatteryThresholdSet = false;
            boolean isRightLowBatteryThresholdSet = false;
            boolean isCaseLowBatteryThresholdSet = false;
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_MAIN_LOW_BATTERY_THRESHOLD) == null) {
                isMainLowBatteryThresholdSet =
                    mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MAIN_LOW_BATTERY_THRESHOLD,
                                                                LOW_BATTERY_THRESHOLD);
            } else {
                isMainLowBatteryThresholdSet = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_LOW_BATTERY_THRESHOLD) == null) {
                isLeftLowBatteryThresholdSet = 
                    mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_LOW_BATTERY_THRESHOLD,
                                                                LOW_BATTERY_THRESHOLD);
            } else {
                isLeftLowBatteryThresholdSet = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_LOW_BATTERY_THRESHOLD) == null) {
                isRightLowBatteryThresholdSet =
                    mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_LOW_BATTERY_THRESHOLD,
                                                                LOW_BATTERY_THRESHOLD);
            } else {
                isRightLowBatteryThresholdSet = true;
            }
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_LOW_BATTERY_THRESHOLD) == null) {
                isCaseLowBatteryThresholdSet =
                    mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_LOW_BATTERY_THRESHOLD,
                                                                LOW_BATTERY_THRESHOLD);
            } else {
                isCaseLowBatteryThresholdSet = true;
            }
            isModelLowBatteryThresholdSet = isMainLowBatteryThresholdSet == true
                                            && isLeftLowBatteryThresholdSet == true
                                            && isRightLowBatteryThresholdSet == true
                                            && isCaseLowBatteryThresholdSet == true;
        }

        if (usingLeft != usingLeftOld) {
            usingLeftOld = usingLeft;
        }
        if (chargingLeft != chargingLeftOld) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_CHARGING,
                                            chargingLeft == true ? TRUE : FALSE);
            chargingLeftOld = chargingLeft;
        }
        batteryTmp = AirPodsUtils.setBatteryLevel(batteryLeft, false);
        batteryNew = ("" + (batteryTmp == -1 ? -1 : batteryTmp*10)).getBytes();
        if (Arrays.equals(batteryNew, LeftBatteryOld) != true) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_LEFT_BATTERY,
                                            batteryNew);
            LeftBatteryOld = batteryNew;
        }

        if (usingRight != usingRightOld) {
            usingRightOld = usingRight;
        }
        if (chargingRight != chargingRightOld) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_CHARGING,
                                            chargingRight == true ? TRUE : FALSE);
            chargingRightOld = chargingRight;
        }
        batteryTmp = AirPodsUtils.setBatteryLevel(batteryRight, false);
        batteryNew = ("" + (batteryTmp == -1 ? -1 : batteryTmp*10)).getBytes();
        if (Arrays.equals(batteryNew, RightBatteryOld) != true) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_RIGHT_BATTERY,
                                            batteryNew);
            RightBatteryOld = batteryNew;
        }

        if (chargingCase != chargingCaseOld) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_CHARGING,
                                            chargingCase == true ? TRUE : FALSE);
            chargingCaseOld = chargingCase;
        }
        batteryTmp = AirPodsUtils.setBatteryLevel(batteryCase, false);
        batteryNew = ("" + (batteryTmp == -1 ? -1 : batteryTmp*10)).getBytes();
        if (Arrays.equals(batteryNew, CaseBatteryOld) != true) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_UNTETHERED_CASE_BATTERY,
                                            batteryNew);
            CaseBatteryOld = batteryNew;
        }

        if (chargingMain != chargingMainOld) {
            mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MAIN_CHARGING,
                                            chargingMain == true ? TRUE : FALSE);
            chargingMainOld = chargingMain;
        }

    }

}
