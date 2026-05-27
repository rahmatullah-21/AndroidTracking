package com.deviceinsight.pro.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Human-readable formatting helpers shared by the UI. */
object Formatters {

    private val timeFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dateTimeFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.getDefault())
    private val dayFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    fun duration(ms: Long): String {
        if (ms <= 0) return "0m"
        val totalMinutes = ms / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "${ms / 1000}s"
        }
    }

    fun bytes(value: Long): String {
        if (value < 1024) return "$value B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var size = value.toDouble() / 1024
        var idx = 0
        while (size >= 1024 && idx < units.lastIndex) {
            size /= 1024
            idx++
        }
        return String.format(Locale.getDefault(), "%.1f %s", size, units[idx])
    }

    fun clockTime(epochMs: Long): String =
        timeFmt.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

    fun dateTime(epochMs: Long): String =
        dateTimeFmt.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

    fun day(epochMs: Long): String =
        dayFmt.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

    fun relative(epochMs: Long, now: Long = System.currentTimeMillis()): String {
        val delta = abs(now - epochMs)
        val mins = delta / 60_000
        return when {
            mins < 1 -> "just now"
            mins < 60 -> "${mins}m ago"
            mins < 1440 -> "${mins / 60}h ago"
            else -> "${mins / 1440}d ago"
        }
    }
}
