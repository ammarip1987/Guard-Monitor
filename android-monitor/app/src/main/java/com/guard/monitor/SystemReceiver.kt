package com.guard.monitor

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

/**
 * Ловит системные широковещательные события: Bluetooth, Wi-Fi, экран вкл/выкл,
 * режим полёта, громкость. Пишет в лог событие + активное приложение.
 */
class SystemReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = ForegroundAppHelper.currentApp(context)
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                val s = when (state) {
                    BluetoothAdapter.STATE_ON -> "включён"
                    BluetoothAdapter.STATE_OFF -> "выключен"
                    BluetoothAdapter.STATE_TURNING_ON -> "включается"
                    BluetoothAdapter.STATE_TURNING_OFF -> "выключается"
                    else -> state.toString()
                }
                Logger.log("BLUETOOTH", "state=$s foreground=$app")
            }

            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
                val s = when (state) {
                    WifiManager.WIFI_STATE_ENABLED -> "включён"
                    WifiManager.WIFI_STATE_DISABLED -> "выключен"
                    WifiManager.WIFI_STATE_ENABLING -> "включается"
                    WifiManager.WIFI_STATE_DISABLING -> "выключается"
                    else -> state.toString()
                }
                Logger.log("WIFI", "state=$s foreground=$app")
            }

            Intent.ACTION_SCREEN_ON -> Logger.log("ЭКРАН", "включён foreground=$app")
            Intent.ACTION_SCREEN_OFF -> Logger.log("ЭКРАН", "выключен")

            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                val on = intent.getBooleanExtra("state", false)
                Logger.log("РЕЖИМ_ПОЛЁТА", "on=$on foreground=$app")
            }

            "android.media.VOLUME_CHANGED_ACTION" ->
                Logger.log("ГРОМКОСТЬ", "изменена foreground=$app")
        }
    }
}
