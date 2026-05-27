package com.deviceinsight.pro.data.source

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over [UsageStatsManager]. Reads foreground time, launch counts and
 * screen-session events, attributing them to human-readable app names.
 */
@Singleton
class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usm =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val pm: PackageManager = context.packageManager

    data class DayUsage(
        val packageName: String,
        val appName: String,
        val totalForegroundMs: Long,
        val launchCount: Int,
        val lastTimeUsed: Long
    )

    data class ScreenTimeRaw(
        val totalForegroundMs: Long,
        val sessionCount: Int,
        val unlockCount: Int,
        val hourlyMs: LongArray
    )

    fun appLabel(packageName: String): String = try {
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (_: Exception) {
        packageName
    }

    /** Aggregated per-app usage between [begin] and [end] (epoch millis). */
    fun queryUsage(begin: Long, end: Long): List<DayUsage> {
        val launchCounts = HashMap<String, Int>()
        runCatching {
            val events = usm.queryEvents(begin, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    launchCounts[event.packageName] = (launchCounts[event.packageName] ?: 0) + 1
                }
            }
        }

        val aggregated = runCatching { usm.queryAndAggregateUsageStats(begin, end) }
            .getOrDefault(emptyMap())

        return aggregated.values
            .filter { it.totalTimeInForeground > 0 || (launchCounts[it.packageName] ?: 0) > 0 }
            .filter { it.packageName != context.packageName }
            .map { stat ->
                DayUsage(
                    packageName = stat.packageName,
                    appName = appLabel(stat.packageName),
                    totalForegroundMs = stat.totalTimeInForeground,
                    launchCount = launchCounts[stat.packageName] ?: 0,
                    lastTimeUsed = stat.lastTimeUsed
                )
            }
            .sortedByDescending { it.totalForegroundMs }
    }

    /** Reconstructs screen sessions and unlocks from the raw event stream. */
    fun queryScreenTime(begin: Long, end: Long): ScreenTimeRaw {
        val hourly = LongArray(24)
        var total = 0L
        var sessions = 0
        var unlocks = 0
        val resumeAt = HashMap<String, Long>()

        runCatching {
            val events = usm.queryEvents(begin, end)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED ->
                        resumeAt[event.packageName] = event.timeStamp

                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        val start = resumeAt.remove(event.packageName)
                        if (start != null && event.timeStamp > start) {
                            val dur = event.timeStamp - start
                            total += dur
                            sessions++
                            val hour = Instant.ofEpochMilli(start)
                                .atZone(ZoneId.systemDefault()).hour
                            hourly[hour] += dur
                        }
                    }

                    // Available on API 28+; harmless on older devices (never emitted).
                    UsageEvents.Event.KEYGUARD_HIDDEN -> unlocks++
                }
            }
        }
        return ScreenTimeRaw(total, sessions, unlocks, hourly)
    }
}
