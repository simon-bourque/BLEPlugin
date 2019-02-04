package me.simon.capstone.bleplugin;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEDevice extends BluetoothGattCallback {

    private BluetoothDevice device;
    private BluetoothGatt gatt;

    private UUID deviceUUID;
    private UUID characteristicUUID;

    private ArrayList<Byte> data;

    public BLEDevice(BluetoothDevice device, UUID uuid) {
        this.device = device;
        this.deviceUUID = uuid;
        data = new ArrayList<>();
    }

    public void connect(Context context, String characteristicUUID) {
        gatt = device.connectGatt(context, true, this);
        this.characteristicUUID = UUID.fromString(characteristicUUID);
    }

    public boolean isConnected() {
        return gatt != null;
    }

    public synchronized boolean hasData() {
        return !data.isEmpty();
    }

    public String getUUID() { return deviceUUID.toString(); }

    public synchronized byte[] popData() {
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < data.size(); ++i) {
            bytes[i] = data.get(i).byteValue();
        }
        data.clear();

        return bytes;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i(BLEPlugin.BLEPLUGIN_TAG, "Connected to GATT server.");
            Log.i(BLEPlugin.BLEPLUGIN_TAG, "Attempting to start service discovery:" + gatt.discoverServices());

        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(BLEPlugin.BLEPLUGIN_TAG, "Disconnected from GATT server.");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> services = gatt.getServices();

            // Search for correct characteristic and set notification
            for (BluetoothGattService service : services) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    gatt.setCharacteristicNotification(characteristic, characteristicUUID.equals(characteristic.getUuid()));
                }
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(characteristicUUID.equals(characteristic.getUuid().toString())){
            gatt.readCharacteristic(characteristic);
        }
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte[] bytes = characteristic.getValue();
            for (int i = 0; i < bytes.length; ++i) {
                data.add(bytes[i]);
            }
        }
    }
}
