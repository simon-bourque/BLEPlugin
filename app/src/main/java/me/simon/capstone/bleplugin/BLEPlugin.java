package me.simon.capstone.bleplugin;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;
import java.util.UUID;

public class BLEPlugin {
    private static BluetoothAdapter bluetoothAdapter;
    private static ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    private static boolean scanning = false;
    private static Handler scanHandler = new Handler();
    private static BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            bluetoothDevices.add(device);
        }
    };

    public static boolean init(Context context, Activity activity) {
       //final BluetoothManager bm = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
       bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Bluetooth not supported
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // TODO extend UnityPlayerActivity to handle this
            //Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //activity.startActivityForResult(enableBTIntent, 1);
            return false;
        }

        return true;
    }

    public static boolean startScan(String[] uuidStrings) {
        if (bluetoothAdapter != null) {

            // Create UUIDs
            UUID[] uuids = new UUID[uuidStrings.length];
            for (int i = 0; i < uuids.length; ++i) {
                uuids[i] = UUID.fromString(uuidStrings[i]);
            }

            // Clear previous results
            bluetoothDevices.clear();

            // Stop scanning after 5 seconds
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, 5000);

            // Start scan
            scanning = true;
            return bluetoothAdapter.startLeScan(uuids, leScanCallback);
        }

        return false;
    }

    public static void stopScan() {
        scanning = false;
        bluetoothAdapter.stopLeScan(leScanCallback);
    }
}