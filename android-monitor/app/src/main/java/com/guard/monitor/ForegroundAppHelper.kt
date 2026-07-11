package com.guard.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Определяет приложение, которое было на переднем плане в момент события.
 * Это ЭВРИСТИКА (без root): событие мог вызвать и фоновый процесс/система,
 * но чаще всего виновник — активное приложение.
 * Требует доступа к «Статистике использования» (Usage Access).
 */
object ForegroundAppHelper {

    fun currentApp(context: Context): String {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val begin = end - 10_000
            val events = usm.queryEvents(begin, end)
            var last = ""
            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    last = e.packageName
                }
            }
            if (last.isEmpty()) "?" else last
        } catch (_: Exception) {
            "?"
        }
    }

    fun hasUsageAccess(context: Context): Boolean {
        return try {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 60_000, end)
            !stats.isNullOrEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
