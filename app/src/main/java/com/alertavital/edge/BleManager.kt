package com.alertavital.edge

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import java.util.*

class BleManager(private val context: Context) {

    companion object {
        private const val TAG = "BleManager"

        // Reemplazar estos UUID con los de microcontrolador
        val SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var gatt: BluetoothGatt? = null

    var onDataReceived: ((String) -> Unit)? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(Build.VERSION_CODES.S)
    fun connect(deviceAddress: String) {
        if (!hasBleConnectPermission()) {
            Log.e(TAG, "No hay permiso BLUETOOTH_CONNECT")
            return
        }

        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (device == null) {
            Log.e(TAG, "❌ Dispositivo no encontrado")
            return
        }

        gatt = device.connectGatt(context, false, gattCallback)
        Log.d(TAG, "🔗 Conectando al dispositivo $deviceAddress")
    }

    /** Desconecta y limpia */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        gatt?.close()
        gatt = null
        Log.d(TAG, "❌ BLE desconectado")
    }

    /** Verifica permisos BLE */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBleConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Callback de BLE */
    private val gattCallback = object : BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, " Conectado")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Desconectado")
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Error descubriendo servicios")
                return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.e(TAG, "Servicio no encontrado")
                return
            }

            val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
            if (characteristic == null) {
                Log.e(TAG, " Característica no encontrada")
                return
            }

            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )
            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)

            Log.d(TAG, "Suscrito a notificaciones de la característica")
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val data = characteristic.value?.joinToString(",") ?: "sin datos"
                Log.d(TAG, " Característica leída: $data")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.value?.joinToString(",") ?: "sin datos"
            Log.d(TAG, "📡 Dato recibido: $data")
            onDataReceived?.invoke(data)  // <-- envia el dato a EdgeService
        }


    }
}
