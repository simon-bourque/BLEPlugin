package me.simon.capstone.bleplugin;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

public class BLEDevice extends BluetoothGattCallback {

    private BluetoothDevice device;
    private BluetoothGatt gatt;

    public BLEDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void connect(Context context) {
        gatt = device.connectGatt(context, true, this);
    }

    public boolean isConnected() {
        return gatt != null;
    }
}
