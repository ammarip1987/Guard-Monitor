package com.guard.monitor

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Пишет события в текстовый лог-файл. Файл лежит в getExternalFilesDir("logs"),
 * то есть доступен при подключении к ПК по USB:
 * Внутренняя память/Android/data/com.guard.monitor/files/logs/guard_log.txt
 */
object Logger {
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    @Volatile
    private var file: File? = null

    fun init(context: Context) {
        if (file != null) return
        val dir = File(context.getExternalFilesDir(null), "logs")
        if (!dir.exists()) dir.mkdirs()
        file = File(dir, "guard_log.txt")
    }

    fun logFile(context: Context): File {
        init(context)
        return file!!
    }

    @Synchronized
    fun log(event: String, detail: String = "") {
        val f = file ?: return
        val line = "${fmt.format(Date())}\t$event\t$detail\n"
        try {
            f.appendText(line)
        } catch (_: Exception) {
        }
    }
}
