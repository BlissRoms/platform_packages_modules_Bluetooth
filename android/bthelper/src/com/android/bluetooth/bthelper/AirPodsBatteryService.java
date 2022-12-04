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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class AirPodsBatteryService extends Service {

    private Context mContext;

    private static final String TAG = "AirPodsBatteryService";

    private static final int DATA_LENGTH_BATTERY = 25;

    private static final long REPORT_DELAY_MS = 500;

    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;

    private BluetoothDevice mCurrentDevice;

    private String mBestLeAddress = null;
    private int mBestLeRssi = -128;
    private long mBestLeLastReported = 0;

    private boolean isChanged = false;
    
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> scanResults) {
            for (ScanResult result : scanResults) {
                handleScanResult(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            handleScanResult(result);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
        mContext = getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        stopScan();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        if (intent != null) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                mCurrentDevice = device;
                startScan();
            }
        }
        return START_STICKY;
    }

    private void startScan() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            Log.w(TAG, "BluetoothAdapter is null, ignored");
            return;
        }

        mScanner = mAdapter.getBluetoothLeScanner();
        if (mScanner == null) {
            Log.w(TAG, "BluetoothLeScanner is null, ignored");
            return;
        }

        final List<ScanFilter> filters = new ArrayList<>();

        final byte[] data = new byte[2 + DATA_LENGTH_BATTERY];
        data[0] = AirPodsConstants.MANUFACTURER_MAGIC;
        data[1] = DATA_LENGTH_BATTERY;

        final byte[] mask = new byte[2 + DATA_LENGTH_BATTERY];
        mask[0] = -1;
        mask[1] = -1;

        filters.add(new ScanFilter.Builder()
                .setManufacturerData(AirPodsConstants.MANUFACTURER_ID, data, mask)
                .build());

        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(REPORT_DELAY_MS)
                .build();

        mScanner.startScan(filters, settings, mScanCallback);
        Log.v(TAG, "startScan");
    }

    private void stopScan() {
        if (mScanner == null) {
            return;
        }
        mScanner.stopScan(mScanCallback);
        mScanner = null;
        mBestLeAddress = null;
        mBestLeRssi = -128;
        Log.v(TAG, "stopScan");
    }

    private void handleScanResult(ScanResult result) {
        if (result == null) {
            return;
        }

        final ScanRecord record = result.getScanRecord();
        if (record == null) {
            return;
        }

        final byte[] data = record.getManufacturerSpecificData(AirPodsConstants.MANUFACTURER_ID);
        if (data == null || Arrays.asList(data).contains(null) || data.length != (2 + DATA_LENGTH_BATTERY)) {
            return;
        }

        final String address = result.getDevice().getAddress();
        final int rssi = result.getRssi();

        final long now = SystemClock.elapsedRealtime();
        if (mBestLeAddress == null) {
            mBestLeAddress = address;
            mBestLeRssi = rssi;
            mBestLeLastReported = now;
            Log.d(TAG, "First result from " + address + ", rssi=" + rssi);
        } else if (mBestLeAddress.equals(address)) {
            mBestLeLastReported = now;
        } else {
            if (rssi >= mBestLeRssi) {
                mBestLeAddress = address;
                mBestLeRssi = rssi;
                mBestLeLastReported = now;
                Log.d(TAG, "Better result from " + address + ", rssi=" + rssi);
            } else if (now - mBestLeLastReported > 5000) {
                mBestLeAddress = address;
                mBestLeRssi = rssi;
                mBestLeLastReported = now;
                Log.d(TAG, "Best result gone, alternate result from " + address + ", rssi=" + rssi);
            } else {
                return;
            }
        }
        
        AirPodsUtils.setModel(data);

        AirPodsUtils.setModelData(data);

        isChanged = AirPodsUtils.isModelStateChanged();

        final Object[] arguments = AirPodsUtils.getModelArguments();

        broadcastVendorSpecificEventIntent(
                BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_IPHONEACCEV,
                BluetoothAssignedNumbers.APPLE,
                BluetoothHeadset.AT_CMD_TYPE_SET,
                arguments,
                mCurrentDevice);
    }

    private void broadcastVendorSpecificEventIntent(String command, int companyId, int commandType,
            Object[] arguments, BluetoothDevice device) {
        final Intent intent = new Intent(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
        intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD, command);
        intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE, commandType);
        // assert: all elements of args are Serializable
        intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS, arguments);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        intent.putExtra(BluetoothDevice.EXTRA_NAME, device.getName());
        intent.addCategory(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY + "."
                + Integer.toString(companyId));
        String[] permissions = new String[] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
        };
        sendBroadcastAsUserMultiplePermissions(intent, UserHandle.ALL, permissions);

        AirPodsUtils.setModelMetaData(mContext, device);

        if (isChanged == true) {
            broadcastStatusChanges(intent);
            isChanged = false;
        }
    }

    // TODO: Fix non-protected broadcast from system error spam
    private void broadcastStatusChanges(Intent intent) {
        final Intent statusIntent = new Intent("batterywidget.impl.action.update_bluetooth_data").setPackage("com.google.android.settings.intelligence");
        statusIntent.putExtra("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED", intent);
        sendBroadcastAsUser(statusIntent, UserHandle.ALL);
    }

}
