package com.guard.monitor

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Сканирует, какие приложения МОГУТ удалённо управлять устройством или следить:
 *  - администраторы устройства (Device Admin) — могут блокировать/сбрасывать/менять политику;
 *  - включённые службы специальных возможностей (Accessibility) — главный вектор
 *    удалённого управления и слежки (могут «нажимать», читать экран);
 *  - приложения с опасными разрешениями (камера, микрофон, SMS, геолокация,
 *    поверх других окон, изменение настроек, доступ к статистике).
 * Всё пишется в лог, чтобы потом просмотреть с ПК.
 */
object SecurityScanner {

    private val SENSITIVE = listOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW,      // рисовать поверх других окон
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_SETTINGS,
        "android.permission.PACKAGE_USAGE_STATS",
        "android.permission.BIND_DEVICE_ADMIN"
    )

    fun scan(context: Context) {
        Logger.log("СКАН", "начало проверки")
        scanDeviceAdmins(context)
        scanAccessibility(context)
        scanSensitiveApps(context)
        Logger.log("СКАН", "проверка завершена")
    }

    private fun scanDeviceAdmins(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admins = dpm.activeAdmins ?: return
            for (a in admins) {
                Logger.log("АДМИН_УСТРОЙСТВА", "app=${a.packageName}")
            }
        } catch (_: Exception) {
        }
    }

    private fun scanAccessibility(context: Context) {
        try {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            for (svc in enabled.split(":")) {
                if (svc.isNotBlank()) Logger.log("СПЕЦ_ВОЗМОЖНОСТИ", "service=$svc")
            }
        } catch (_: Exception) {
        }
    }

    private fun scanSensitiveApps(context: Context) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (pkg in packages) {
                if (isSystem(pkg)) continue
                val requested = pkg.requestedPermissions ?: continue
                val hits = requested.filter { it in SENSITIVE }
                if (hits.isNotEmpty()) {
                    Logger.log(
                        "ПРИЛОЖЕНИЕ_РАЗРЕШЕНИЯ",
                        "app=${pkg.packageName} perms=${hits.joinToString(",") { it.substringAfterLast('.') }}"
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun isSystem(pkg: PackageInfo): Boolean {
        val ai = pkg.applicationInfo ?: return false
        return (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}
