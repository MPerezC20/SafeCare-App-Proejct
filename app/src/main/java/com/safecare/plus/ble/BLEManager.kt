package com.safecare.plus.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

@SuppressLint("MissingPermission")
class BLEManager(private val context: Context, private val listener: BLEListener) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false

    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private val CHARACTERISTIC_UUID = UUID.fromString("abcd1234-5678-1234-5678-abcdef123456")
    private val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    interface BLEListener {
        fun onSOSAlertReceived()
        fun onDeviceConnected()
        fun onDeviceDisconnected()
    }

    fun startScanning() {
        if (isScanning || bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.d("BLEManager", "Escaneo no iniciado. Bluetooth habilitado: ${bluetoothAdapter?.isEnabled}")
            return
        }

        val scanner = bluetoothAdapter!!.bluetoothLeScanner
        if (scanner == null) {
            Log.e("BLEManager", "No se pudo obtener el BluetoothLeScanner")
            return
        }

        // Usamos un escaneo abierto (sin filtros) para máxima compatibilidad entre dispositivos
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        Log.d("BLEManager", "Iniciando escaneo abierto para detectar ESP32_SOS...")
        isScanning = true
        scanner.startScan(null, settings, scanCallback)
        
        // El escaneo se detendrá después de 30 segundos
        handler.postDelayed({
            if (isScanning) stopScanning()
        }, 30000)
    }

    fun stopScanning() {
        if (!isScanning || bluetoothAdapter == null) return
        Log.d("BLEManager", "Deteniendo escaneo")
        bluetoothAdapter!!.bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            // Buscamos el nombre tanto en el objeto device como en el paquete de anuncio (ScanRecord)
            val deviceName = device.name ?: result.scanRecord?.deviceName ?: "Desconocido"
            
            // Log para debuggear en el Logcat qué dispositivos está viendo tu móvil
            Log.d("BLEManager", "Dispositivo visto: $deviceName - ${device.address}")

            // Filtro manual: por nombre exacto o por el UUID del servicio en el anuncio
            val hasService = result.scanRecord?.serviceUuids?.any { it.uuid == SERVICE_UUID } ?: false

            if (deviceName == "ESP32_SOS" || hasService) {
                Log.d("BLEManager", "¡Dispositivo SOS detectado! Conectando a ${device.address}")
                stopScanning()
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLEManager", "Error en escaneo: $errorCode")
            isScanning = false
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d("BLEManager", "Iniciando conexión GATT...")
        // Pequeño delay de 500ms para estabilizar el stack Bluetooth antes de conectar
        handler.postDelayed({
            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, gattCallback)
            }
        }, 500)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLEManager", "GATT Conectado. Descubriendo servicios...")
                listener.onDeviceConnected()
                // Retraso de 1 segundo para asegurar estabilidad antes de descubrir servicios
                handler.postDelayed({ gatt.discoverServices() }, 1000)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLEManager", "GATT Desconectado. Status: $status")
                listener.onDeviceDisconnected()
                bluetoothGatt?.close()
                bluetoothGatt = null
                // Reintento automático de escaneo tras 5 segundos
                handler.postDelayed({ startScanning() }, 5000)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEManager", "Servicios descubiertos con éxito")
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
                if (characteristic != null) {
                    subscribeToNotifications(gatt, characteristic)
                } else {
                    Log.e("BLEManager", "Servicio o Característica SOS no encontrados en el dispositivo")
                }
            } else {
                Log.e("BLEManager", "Fallo al descubrir servicios: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val data = characteristic.value?.let { String(it) } ?: ""
                Log.d("BLEManager", "Notificación recibida del botón: $data")
                
                // Flexible: detectamos si el mensaje contiene "Alerta" (tu código ESP32) o "SOS"
                if (data.contains("Alerta", ignoreCase = true) || data.contains("SOS", ignoreCase = true)) {
                    listener.onSOSAlertReceived()
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEManager", "Suscripción a notificaciones SOS confirmada")
            }
        }
    }

    private fun subscribeToNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val success = gatt.setCharacteristicNotification(characteristic, true)
        if (success) {
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                Log.d("BLEManager", "Solicitando notificaciones para la característica SOS...")
            }
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}