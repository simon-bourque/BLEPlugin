package me.simon.capstone.bleplugin;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;

public class BLEDevice extends BluetoothGattCallback {

    private BluetoothDevice device;
    private BluetoothGatt gatt;

    private UUID deviceUUID;
    private UUID characteristicUUID;

    private byte[] data = null;

    public BLEDevice(BluetoothDevice device, UUID uuid) {
        this.device = device;
        this.deviceUUID = uuid;
    }

    public void connect(Context context, String characteristicUUID) {
        gatt = device.connectGatt(context, true, this);
        this.characteristicUUID = UUID.fromString(characteristicUUID);
    }

    public boolean isConnected() {
        return BLEPlugin.isConnected(device);
    }

    public synchronized boolean hasData() {
        return data != null;
    }

    public String getUUID() { return deviceUUID.toString(); }

    public synchronized byte[] popData() {
        byte[] bytes = data;
        data = null;
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
                if(service.getUuid().toString().equals("6e400001-b5a3-f393-e0a9-e50e24dcca9e")){
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if(characteristicUUID.equals(characteristic.getUuid())){
                            Log.i(BLEPlugin.BLEPLUGIN_TAG, "Setting notification on for: " + characteristicUUID.toString());
                            gatt.setCharacteristicNotification(characteristic, true);

                            int result = characteristic.getProperties();

                            if(( result & BluetoothGattCharacteristic.PROPERTY_READ) != 0){
                                Log.i(BLEPlugin.BLEPLUGIN_TAG, "We got read");
                            }
                            if((result & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0){
                                Log.i(BLEPlugin.BLEPLUGIN_TAG, "We got notify");
                            }

                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("000002902-0000-1000-8000-00805f9b34fb"));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(characteristicUUID.equals(characteristic.getUuid())){
            data = characteristic.getValue();
            //gatt.readCharacteristic(characteristic);
        }
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            data = characteristic.getValue();
        }
    }
}
