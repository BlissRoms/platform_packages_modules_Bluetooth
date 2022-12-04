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

public class Generic {

    private static final byte[] LOW_BATTERY_THRESHOLD = (""+20).getBytes();

    private static final int FLAG_REVERSED = 1 << 7;

    private static boolean isModelLowBatteryThresholdSet = false;

    private static int batteryLeft, batteryRight, batteryUnified;
    private static boolean rightLeft;
    
    public static void setModelData(byte[] data) {
        final int flags = data[5];
        final int battery = data[6];

        rightLeft = ((flags & FLAG_REVERSED) != 0);

        if (rightLeft == false) {
            batteryLeft = (battery >> 4) & 0xf;
            batteryRight = battery & 0xf;
        } else {
            batteryLeft = battery & 0xf;
            batteryRight = (battery >> 4) & 0xf;
        }

        // Log.d(TAG, String.format("%s\t%s\tL: %s (%s)\tR: %s (%s)\tCASE: %s (%s)",
        //         address, rightLeft,
        //         batteryLeft == 0xf ? "-" : batteryLeft, usingLeft ? "USE" : chargingLeft ? "CHG" : "---",
        //         batteryRight == 0xf ? "-" : batteryRight, usingRight ? "USE" : chargingRight ? "CHG" : "---",
        //         batteryCase == 0xf ? "-" : batteryCase, chargingCase ? "CHG" : "---"));

        batteryUnified = Math.min(batteryLeft, batteryRight);
    }

    public static boolean isModelStateChanged() {
        return false;
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
        if (isModelLowBatteryThresholdSet == false) {
            boolean isMainLowBatteryThresholdSet = false;
            if (mCurrentDevice.getMetadata(BluetoothDevice.METADATA_MAIN_LOW_BATTERY_THRESHOLD) == null) {
                isMainLowBatteryThresholdSet =
                    mCurrentDevice.setMetadata(BluetoothDevice.METADATA_MAIN_LOW_BATTERY_THRESHOLD,
                                                                LOW_BATTERY_THRESHOLD);
            } else {
                isMainLowBatteryThresholdSet = true;
            }
            isModelLowBatteryThresholdSet = isMainLowBatteryThresholdSet == true;
        }
    }

}
