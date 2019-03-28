package me.simon.capstone.bleplugin;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEPlugin {
    public static final String BLEPLUGIN_TAG = "BLEPLUGIN";

    private static BluetoothManager bm;
    private static BluetoothAdapter bluetoothAdapter;
    private static ArrayList<BLEDevice> discoveredDevices = new ArrayList<>();

    private static boolean scanning = false;
    private static Handler scanHandler = new Handler();

    private static ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.v(BLEPLUGIN_TAG, result.toString());
            discoveredDevices.add(new BLEDevice(result.getDevice(), result.getScanRecord().getServiceUuids().get(0).getUuid()));
        }
        @Override
        public void onScanFailed(int errorCode){
            Log.v(BLEPLUGIN_TAG, "Scan failed with error: " + errorCode);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results){
            Log.v(BLEPLUGIN_TAG,"BATCHES");
        }
    };

    public static boolean init(Context context, Activity activity) {
        bm = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);

        Log.v(BLEPLUGIN_TAG, "Initializing plugin!");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();

        if (bluetoothAdapter == null) {
            // Bluetooth not supported
            Log.v(BLEPLUGIN_TAG, "Bluetooth not supported!");
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // TODO extend UnityPlayerActivity to handle this
            //Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //activity.startActivityForResult(enableBTIntent, 1);
            Log.v(BLEPLUGIN_TAG, "Bluetooth not enabled!");
            return false;
        }

        return true;
    }

    public static void startScan(String[] uuidStrings) {
        if (bluetoothAdapter != null) {

            // Create UUIDs
            ParcelUuid[] uuids = new ParcelUuid[uuidStrings.length];
            for (int i = 0; i < uuids.length; ++i) {
                uuids[i] = ParcelUuid.fromString(uuidStrings[i]);
            }

            // Clear previous results
            discoveredDevices.clear();

            // Stop scanning after 5 seconds
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, 10000);

            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
            ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();

            settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH);

            for(int i = 0; i < uuids.length; i++){
                filterBuilder.setServiceUuid(uuids[i]);
                scanFilters.add(filterBuilder.build());
            }

            // Start scan
            Log.v(BLEPLUGIN_TAG, "Starting scan!");
            scanning = true;
            bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, settingsBuilder.build(), scanCallback);
        }
    }

    public static void stopScan() {
        Log.v(BLEPLUGIN_TAG, "Stopping scan!");
        scanning = false;
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    public static boolean isConnected(BluetoothDevice device) {
        List<BluetoothDevice> connectedDevices = bm.getConnectedDevices(BluetoothGatt.GATT);
        return connectedDevices.contains(device);
    }

    public static boolean isScanning() {
        return scanning;
    }

    public static ArrayList<BLEDevice> getDiscoveredDevices() {
        return discoveredDevices;
    }
}