package com.guard.monitor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logger.init(this)

        findViewById<Button>(R.id.btnUsage).setOnClickListener {
            openSafely(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        }
        findViewById<Button>(R.id.btnNotif).setOnClickListener {
            openSafely(Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                Settings.EXTRA_APP_PACKAGE to packageName)
        }
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val i = Intent(this, MonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
            else startService(i)
            toast("Мониторинг запущен")
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, MonitorService::class.java))
            toast("Мониторинг остановлен")
        }
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            Thread { SecurityScanner.scan(this) }.start()
            toast("Скан запущен — смотри лог")
        }

        findViewById<TextView>(R.id.txtPath).text =
            "Лог-файл:\n" + Logger.logFile(this).absolutePath
    }

    override fun onResume() {
        super.onResume()
        val ok = ForegroundAppHelper.hasUsageAccess(this)
        findViewById<TextView>(R.id.txtStatus).text =
            if (ok) "Доступ к статистике: ЕСТЬ (виновник определяется)"
            else "Доступ к статистике: НЕТ — нажми «Доступ к статистике» и включи"
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    private fun openSafely(action: String, vararg extras: Pair<String, String>) {
        try {
            val i = Intent(action)
            for ((k, v) in extras) i.putExtra(k, v)
            startActivity(i)
        } catch (_: Exception) {
            toast("Не удалось открыть настройки")
        }
    }
}
