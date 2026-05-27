package com.deviceinsight.pro.utils

import java.time.LocalDate
import java.time.ZoneId

/** Helpers for converting between calendar days and epoch boundaries. */
object TimeWindows {

    fun todayEpochDay(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).toEpochDay()

    fun startOfDayMs(epochDay: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDayMs(epochDay: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.ofEpochDay(epochDay).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    /** Start-of-day epoch millis for `daysBack` days ago (0 = today). */
    fun startOfDaysAgoMs(daysBack: Int, zone: ZoneId = ZoneId.systemDefault()): Long =
        startOfDayMs(todayEpochDay(zone) - daysBack, zone)

    fun epochDayOf(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        java.time.Instant.ofEpochMilli(epochMs).atZone(zone).toLocalDate().toEpochDay()
}
