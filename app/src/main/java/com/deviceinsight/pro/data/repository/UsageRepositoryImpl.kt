package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.source.UsageStatsDataSource
import com.deviceinsight.pro.database.dao.AppUsageDao
import com.deviceinsight.pro.database.entity.AppUsageEntity
import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.domain.repository.UsageRepository
import com.deviceinsight.pro.utils.PermissionUtils
import com.deviceinsight.pro.utils.TimeWindows
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class UsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageDao: AppUsageDao,
    private val dataSource: UsageStatsDataSource
) : UsageRepository {

    override fun observeUsage(days: Int): Flow<List<AppUsageInfo>> {
        val today = TimeWindows.todayEpochDay()
        val flow = if (days <= 1) {
            usageDao.observeForDay(today)
        } else {
            usageDao.observeRange(today - (days - 1), today)
        }
        return flow.map { rows -> aggregate(rows) }
    }

    override fun observeScreenTime(days: Int): Flow<ScreenTimeStats> {
        val today = TimeWindows.todayEpochDay()
        // Re-derive screen time from the raw event stream whenever stored usage changes.
        return usageDao.observeRange(today - (days - 1).coerceAtLeast(0), today)
            .map { computeScreenTime(days) }
            .flowOn(Dispatchers.IO)
    }

    override fun observeTotalForegroundToday(): Flow<Long> =
        usageDao.observeTotalForegroundForDay(TimeWindows.todayEpochDay())

    override fun observeDailyForeground(days: Int): Flow<List<Long>> {
        val today = TimeWindows.todayEpochDay()
        val start = today - (days - 1).coerceAtLeast(0)
        return usageDao.observeRange(start, today).map { rows ->
            val totals = LongArray(days)
            rows.forEach { row ->
                val idx = (row.dateEpochDay - start).toInt()
                if (idx in 0 until days) totals[idx] += row.totalForegroundMs
            }
            totals.toList()
        }
    }

    override suspend fun syncUsage(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!hasUsageAccess()) error("Usage access not granted")
            val today = TimeWindows.todayEpochDay()
            val begin = TimeWindows.startOfDayMs(today)
            val end = System.currentTimeMillis()
            val usage = dataSource.queryUsage(begin, end)
            val rows = usage.map { u ->
                val existing = usageDao.find(u.packageName, today)
                AppUsageEntity(
                    id = existing?.id ?: 0L,
                    packageName = u.packageName,
                    appName = u.appName,
                    dateEpochDay = today,
                    totalForegroundMs = u.totalForegroundMs,
                    launchCount = u.launchCount,
                    lastTimeUsed = u.lastTimeUsed
                )
            }
            usageDao.upsertAll(rows)
        }
    }

    override fun hasUsageAccess(): Boolean = PermissionUtils.hasUsageAccess(context)

    private fun aggregate(rows: List<AppUsageEntity>): List<AppUsageInfo> =
        rows.groupBy { it.packageName }
            .map { (pkg, group) ->
                AppUsageInfo(
                    packageName = pkg,
                    appName = group.maxByOrNull { it.dateEpochDay }?.appName ?: pkg,
                    totalForegroundMs = group.sumOf { it.totalForegroundMs },
                    launchCount = group.sumOf { it.launchCount },
                    lastTimeUsed = group.maxOf { it.lastTimeUsed }
                )
            }
            .sortedByDescending { it.totalForegroundMs }

    private fun computeScreenTime(days: Int): ScreenTimeStats {
        val today = TimeWindows.todayEpochDay()
        val begin = TimeWindows.startOfDayMs(today - (days - 1).coerceAtLeast(0))
        val end = System.currentTimeMillis()
        val raw = dataSource.queryScreenTime(begin, end)

        val avgSession = if (raw.sessionCount > 0) raw.totalForegroundMs / raw.sessionCount else 0L
        val windowMs = max(0L, end - begin)
        val idle = max(0L, windowMs - raw.totalForegroundMs)
        val peakHour = raw.hourlyMs.indices.maxByOrNull { raw.hourlyMs[it] } ?: 0

        // Focus score: fewer unlocks and longer average sessions => higher focus.
        val penalty = raw.unlockCount + raw.sessionCount / 2
        val focus = (100 - penalty).coerceIn(0, 100)

        return ScreenTimeStats(
            totalScreenOnMs = raw.totalForegroundMs,
            unlockCount = raw.unlockCount,
            averageSessionMs = avgSession,
            idleTimeMs = idle,
            peakHour = peakHour,
            focusScore = focus,
            hourlyUsageMs = raw.hourlyMs.toList()
        )
    }
}
