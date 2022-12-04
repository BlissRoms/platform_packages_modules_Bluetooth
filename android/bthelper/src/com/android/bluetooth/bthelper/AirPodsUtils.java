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

package com.android.bluetooth.bthelper;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.util.Log;

import com.android.bluetooth.bthelper.models.*;

public class AirPodsUtils {

    private static final String TAG = "AirPodsUtils";

    private static String model = null;
    private static String modelPartial = null;

    public static Uri resToUri(Context context, int resId, String[] packageNames) {
        try {
            Uri uri = (new Uri.Builder())
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(context.getResources().getResourcePackageName(resId))
                        .appendPath(context.getResources().getResourceTypeName(resId))
                        .appendPath(context.getResources().getResourceEntryName(resId))
                        .build();

            for (String packages: packageNames) {
                context.grantUriPermission(
                    packages,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            return uri;
        } catch (NotFoundException e) {
            Log.d(TAG, "NotFoundException: " + e);
            return null;
        }
    }

    public static void setModel(byte[] data) {
        model = ""+((data[3] >> 4) & 0xF)+(data[3] & 0xF)+((data[4] >> 4) & 0xF)+(data[4] & 0xF);
        modelPartial = ""+(data[3] & 0xF);
        Log.d(TAG, "Model code: "+model);
    }

    public static void setModelData(byte[] data) {
        //switch (modelPartial) {
        //    case "10": AirPodsMax.setModelData(data); break;  // Need full model code
        //}
        switch (model) {
            case "0220": AirPods.setModelData(data); break;
            case "01520": AirPodsGen2.setModelData(data); break;
            case "1320": AirPodsGen3.setModelData(data); break;
            case "01420": AirPodsPro.setModelData(data); break;
            case "1420": AirPodsProGen2.setModelData(data); break;
            //case "X11XX": PowerbeatsPro.setModelData(data); // Need full model code // No Icon yet
            //case "0520": BeatsX.setModelData(data); // No Icon yet
            //case "1020": BeatsFlex.setModelData(data); // No Icon yet
            //case "0620": BeatsSolo3.setModelData(data); // No Icon yet
            //case "X9XX": BeatsStudio3.setModelData(data); // Need full model code // No Icon yet
            //case "0320": Powerbeats3.setModelData(data); // No Icon yet
            default: Generic.setModelData(data); break;
        }
    }

    public static Object[] getModelArguments() {
        //switch (modelPartial) {
        //    case "10": return AirPodsMax.getModelArguments(); // Need full model code
        //}
        switch (model) {
            case "0220": return AirPods.getModelArguments();
            case "01520": return AirPodsGen2.getModelArguments();
            case "1320": return AirPodsGen3.getModelArguments();
            case "01420": return AirPodsPro.getModelArguments();
            case "1420": return AirPodsProGen2.getModelArguments();
            //case "X11XX": return PowerbeatsPro.getModelArguments(); // Need full model code // No Icon yet
            //case "0520": return BeatsX.getModelArguments(); // No Icon yet
            //case "1020": return BeatsFlex.getModelArguments(); // No Icon yet
            //case "0620": return BeatsSolo3.getModelArguments(); // No Icon yet
            //case "X9XX": return BeatsStudio3.getModelArguments(); // Need full model code // No Icon yet
            //case "0320": return Powerbeats3.getModelArguments(); // No Icon yet
            default: return Generic.getModelArguments();
        }
    }

    public static void setModelMetaData(Context context, BluetoothDevice mCurrentDevice) {
        //switch (modelPartial) {
        //    case "10": AirPodsMax.setModelMetaData(context, mCurrentDevice); break;  // Need full model code
        //}
        switch (model) {
            case "0220": AirPods.setModelMetaData(context, mCurrentDevice); break;
            case "01520": AirPodsGen2.setModelMetaData(context, mCurrentDevice); break;
            case "1320": AirPodsGen3.setModelMetaData(context, mCurrentDevice); break;
            case "01420": AirPodsPro.setModelMetaData(context, mCurrentDevice); break;
            case "1420": AirPodsProGen2.setModelMetaData(context, mCurrentDevice); break;
            //case "X11XX": PowerbeatsPro.setModelMetaData(context, mCurrentDevice); // Need full model code
            //case "0520": BeatsX.setModelMetaData(context, mCurrentDevice); // No Icon yet
            //case "1020": BeatsFlex.setModelMetaData(context, mCurrentDevice); // No Icon yet
            //case "0620": BeatsSolo3.setModelMetaData(context, mCurrentDevice); // No Icon yet
            //case "X9XX": BeatsStudio3.setModelMetaData(context, mCurrentDevice); // Need full model code // No Icon yet
            //case "0320": Powerbeats3.setModelMetaData(context, mCurrentDevice); // No Icon yet
            default: Generic.setModelMetaData(context, mCurrentDevice); break;
        }
    }

    public static boolean isModelStateChanged() {
        //switch (modelPartial) {
        //    case "10": return AirPodsMax.isModelStateChanged(); // Need full model code
        //}
        switch (model) {
            case "0220": return AirPods.isModelStateChanged();
            case "01520": return AirPodsGen2.isModelStateChanged();
            case "1320": return AirPodsGen3.isModelStateChanged();
            case "01420": return AirPodsPro.isModelStateChanged();
            case "1420": return AirPodsProGen2.isModelStateChanged();
            //case "X11XX": return PowerbeatsPro.isModelStateChanged(); // Need full model code
            //case "0520": return BeatsX.isModelStateChanged(); // No Icon yet
            //case "1020": return BeatsFlex.isModelStateChanged(); // No Icon yet
            //case "0620": return BeatsSolo3.isModelStateChanged(); // No Icon yet
            //case "X9XX": return BeatsStudio3.isModelStateChanged(); // Need full model code // No Icon yet
            //case "0320": return Powerbeats3.isModelStateChanged(); // No Icon yet
            default: return false; // Generic.isModelStateChanged();
        }
    }

    public static int setBatteryLevel(int battery, boolean isArgument) {
        if (battery == 15) { // [0, 10] and 15 (Disconnected)
            return BluetoothDevice.BATTERY_LEVEL_UNKNOWN; // -1
        } else if (battery > 10) {
            battery = 10;
        }
        if (isArgument == true) {
            if (battery > 0) {
                battery -= 1; // [0, 9]
            }
        }
        if (battery < 0) {
            return BluetoothDevice.BATTERY_LEVEL_UNKNOWN;
        }
        return battery;
    }

}
