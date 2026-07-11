package com.guard.monitor

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings

/**
 * Следит за изменениями системных настроек: яркость, авто-яркость, тайм-аут экрана,
 * режим геолокации. На каждое изменение пишет в лог значение и активное приложение.
 */
class SettingsObservers(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private val observers = mutableListOf<ContentObserver>()

    private fun readSys(key: String): String =
        try {
            Settings.System.getInt(context.contentResolver, key).toString()
        } catch (_: Exception) {
            "?"
        }

    private fun readSecure(key: String): String =
        try {
            Settings.Secure.getInt(context.contentResolver, key).toString()
        } catch (_: Exception) {
            "?"
        }

    private fun watch(uri: Uri, name: String, valueReader: () -> String) {
        val obs = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                val app = ForegroundAppHelper.currentApp(context)
                Logger.log(name, "value=${valueReader()} foreground=$app")
            }
        }
        context.contentResolver.registerContentObserver(uri, false, obs)
        observers.add(obs)
    }

    fun start() {
        watch(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            "ЯРКОСТЬ"
        ) { readSys(Settings.System.SCREEN_BRIGHTNESS) }

        watch(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
            "ЯРКОСТЬ_АВТО"
        ) { readSys(Settings.System.SCREEN_BRIGHTNESS_MODE) }

        watch(
            Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT),
            "ТАЙМАУТ_ЭКРАНА"
        ) { readSys(Settings.System.SCREEN_OFF_TIMEOUT) }

        @Suppress("DEPRECATION")
        watch(
            Settings.Secure.getUriFor(Settings.Secure.LOCATION_MODE),
            "ГЕОЛОКАЦИЯ"
        ) { readSecure(Settings.Secure.LOCATION_MODE) }
    }

    fun stop() {
        for (o in observers) {
            try {
                context.contentResolver.unregisterContentObserver(o)
            } catch (_: Exception) {
            }
        }
        observers.clear()
    }
}
