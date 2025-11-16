package com.alertavital.edge

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class EdgeService : Service() {

    private var running = true
    private lateinit var cloudClient: CloudClient
    private lateinit var scheduler: ScheduledExecutorService

    override fun onCreate() {
        super.onCreate()
        Log.d("EdgeService", "Servicio creado")

        try {
            startForeground(5, createNotification())
            Log.d("EdgeService", "Foreground iniciado")

            cloudClient = CloudClient("ws://10.0.2.2:8081/ws/monitoring") // <--- Cambia la IP/puerto
            cloudClient.connect()

            scheduler = Executors.newSingleThreadScheduledExecutor()
            scheduler.scheduleWithFixedDelay(
                {
                    try {
                        if (!running) return@scheduleWithFixedDelay
                        val bpm = (60..100).random()
                        val spo2 = (95..100).random()
                        val dataJson = "{\"bpm\": $bpm, \"spo2\": $spo2}"
                        cloudClient.sendData(dataJson)
                    } catch (e: Exception) {
                        Log.e("EdgeService", "Error enviando dato: ${e.message}")
                    }
            }, 0, 2, TimeUnit.SECONDS)  // 2 segundos de delay entre el final de un ciclo y el inicio del siguiente


        } catch (e: Exception) {
            Log.e("EdgeService", "Error en onCreate: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("EdgeService", "Servicio iniciado correctamente")
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        scheduler.shutdown()
        cloudClient.disconnect()
        super.onDestroy()
        Log.d("EdgeService", "Servicio detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "edge_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Edge Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Edge Mobile Gateway activo")
            .setContentText("Procesando datos IoT simulados...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .build()
    }
}

/*
import android.Manifest
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat

class EdgeService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("EdgeService", "Servicio iniciado correctamente")

        return START_STICKY
    }
    private lateinit var bleManager: BleManager
    private lateinit var cloudClient: CloudClient

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate() {
        super.onCreate()
        Log.d("EdgeService", "Servicio creado")

        val notification = createNotification()
        startForeground(1, notification)

        // Inicializar BLE y CloudClient
        bleManager = BleManager(this)
        cloudClient = CloudClient("wss://tu-backend.com/ws")
        cloudClient.connect()

        // Conectar al dispositivo BLE (coloca la dirección correcta)
        bleManager.connect("XX:XX:XX:XX:XX:XX")

        // Subscribir datos BLE y enviarlos al backend
        bleManager.onDataReceived = { data ->
            Log.d("EdgeService", "Dato BLE recibido: $data -> enviando a backend")
            cloudClient.sendData(data)
        }

        /*
        // Aquí luego se conectará con BLE y el backend
        Thread {
            repeat(10) {
                Log.d("EdgeService", "Simulando dato: ${60 + it} BPM")
                Thread.sleep(2000)
            }
        }.start()
        */

    }


    override fun onDestroy() {
        bleManager.disconnect()
        cloudClient.disconnect()
        super.onDestroy()
        Log.d("EdgeService", "Servicio detenido")

    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "edge_channel"

        val channel = NotificationChannel(
            channelId,
            "Edge Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Edge Mobile Gateway activo")
            .setContentText("Procesando datos IoT...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .build()
    }
}

*/