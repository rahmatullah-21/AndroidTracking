package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.data.source.NetworkStatsDataSource
import com.deviceinsight.pro.database.dao.NetworkUsageDao
import com.deviceinsight.pro.database.entity.NetworkUsageEntity
import com.deviceinsight.pro.domain.model.NetworkDaySummary
import com.deviceinsight.pro.domain.model.NetworkSpeed
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.utils.TimeWindows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    private val dataSource: NetworkStatsDataSource,
    private val dao: NetworkUsageDao
) : NetworkRepository {

    override fun observeDailyUsage(days: Int): Flow<List<NetworkDaySummary>> {
        val today = TimeWindows.todayEpochDay()
        return dao.observeRange(today - (days - 1).coerceAtLeast(0), today)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeRealtimeSpeed(): Flow<NetworkSpeed> = dataSource.realtimeSpeed()

    override suspend fun syncToday(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val today = TimeWindows.todayEpochDay()
            val begin = TimeWindows.startOfDayMs(today)
            val end = System.currentTimeMillis()
            val wifi = dataSource.wifiTotals(begin, end)
            val mobile = dataSource.mobileTotals(begin, end)
            val existing = dao.find(today)
            dao.upsert(
                NetworkUsageEntity(
                    id = existing?.id ?: 0L,
                    dateEpochDay = today,
                    wifiRxBytes = wifi.rxBytes,
                    wifiTxBytes = wifi.txBytes,
                    mobileRxBytes = mobile.rxBytes,
                    mobileTxBytes = mobile.txBytes,
                    timestamp = end
                )
            )
        }
    }
}
