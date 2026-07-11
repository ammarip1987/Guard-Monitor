package com.guard.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

/**
 * Постоянный foreground-сервис: держит мониторинг активным, регистрирует приёмники
 * системных событий и наблюдателей настроек, периодически запускает скан безопасности.
 */
class MonitorService : Service() {

    private val receiver = SystemReceiver()
    private var observers: SettingsObservers? = null
    private val handler = Handler(Looper.getMainLooper())

    private val scanRunnable = object : Runnable {
        override fun run() {
            Thread { SecurityScanner.scan(this@MonitorService) }.start()
            handler.postDelayed(this, 60L * 60L * 1000L) // раз в час
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        startForegroundNotification()

        val f = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        registerReceiver(receiver, f)

        observers = SettingsObservers(this).also { it.start() }

        Logger.log("СЕРВИС", "мониторинг запущен")
        handler.post(scanRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        observers?.stop()
        handler.removeCallbacks(scanRunnable)
        Logger.log("СЕРВИС", "мониторинг остановлен")
        super.onDestroy()
    }

    private fun startForegroundNotification() {
        val channelId = "guard_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                channelId, "Guard Monitor", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        val n: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Guard Monitor")
            .setContentText("Мониторинг активности устройства")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .build()
        startForeground(1, n)
    }
}
