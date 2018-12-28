package com.burns.android.ancssample;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.*;

class MidiGattService extends BluetoothGattService {

    public static final UUID MIDI_SERVICE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700");
    public static final UUID MIDI_IO_CHARACTERISTIC_UUID = UUID.fromString("7772E5DB-3868-4112-A1A9-F2669D106BF3");

    public MidiGattService() {
        super(MIDI_SERVICE_UUID, SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic ioCharacteristic = new BluetoothGattCharacteristic(MIDI_IO_CHARACTERISTIC_UUID, PROPERTY_READ | PROPERTY_WRITE_NO_RESPONSE | PROPERTY_NOTIFY, PERMISSION_READ | PERMISSION_READ_ENCRYPTED | PERMISSION_WRITE | PERMISSION_WRITE_ENCRYPTED);
        addCharacteristic(ioCharacteristic);
    }
}
