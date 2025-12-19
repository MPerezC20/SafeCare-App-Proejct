package com.safecare.plus.ble

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.safecare.plus.HomeActivity
import com.safecare.plus.SOSAlertActivity
import com.safecare.plus.R

class BLEService : Service(), BLEManager.BLEListener {

    private lateinit var bleManager: BLEManager
    private val CHANNEL_ID = "SOS_ALERT_CHANNEL"
    private val FOREGROUND_CHANNEL_ID = "BLE_SERVICE_CHANNEL"
    private val NOTIFICATION_ID = 1
    private val SOS_NOTIFICATION_ID = 2

    enum class BLEState {
        IDLE, SCANNING, CONNECTED, DISCONNECTED
    }

    companion object {
        private val _bleStatus = MutableLiveData<BLEState>(BLEState.IDLE)
        val bleStatus: LiveData<BLEState> = _bleStatus
    }

    override fun onCreate() {
        super.onCreate()
        bleManager = BLEManager(this, this)
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createServiceNotification())
        startScanning()
    }

    private fun startScanning() {
        _bleStatus.postValue(BLEState.SCANNING)
        bleManager.startScanning()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSOSAlertReceived() {
        // Lanzar la actividad de alerta a pantalla completa (como una llamada)
        val intent = Intent(this, SOSAlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
        
        // También mostramos la notificación por si la pantalla está bloqueada
        showSOSNotification()
    }

    override fun onDeviceConnected() {
        Log.d("BLEService", "Device Connected")
        _bleStatus.postValue(BLEState.CONNECTED)
        updateServiceNotification("ESP32_SOS Conectado")
    }

    override fun onDeviceDisconnected() {
        Log.d("BLEService", "Device Disconnected")
        _bleStatus.postValue(BLEState.DISCONNECTED)
        updateServiceNotification("Buscando ESP32_SOS...")
        _bleStatus.postValue(BLEState.SCANNING)
    }

    private fun createServiceNotification(): Notification {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("SafeCare BLE")
            .setContentText("Buscando dispositivo SOS...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateServiceNotification(text: String) {
        val notification = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("SafeCare BLE")
            .setContentText(text)
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showSOSNotification() {
        val fullScreenIntent = Intent(this, SOSAlertActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("¡ALERTA SOS!")
            .setContentText("Se ha activado el botón de emergencia")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Esto hace que salte el pop-up
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SOS_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "SafeCare Service",
                NotificationManager.IMPORTANCE_LOW
            )
            
            val sosChannel = NotificationChannel(
                CHANNEL_ID,
                "SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para alertas de emergencia SOS"
                enableLights(true)
                enableVibration(true)
                val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                vibrationPattern = pattern
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(sosChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.disconnect()
        _bleStatus.postValue(BLEState.IDLE)
    }
}