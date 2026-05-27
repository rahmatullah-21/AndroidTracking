package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.mapper.toSample
import com.deviceinsight.pro.data.source.SystemMetricsDataSource
import com.deviceinsight.pro.database.dao.BatteryStatDao
import com.deviceinsight.pro.database.entity.BatteryStatEntity
import com.deviceinsight.pro.domain.model.BatteryInfo
import com.deviceinsight.pro.domain.model.BatterySample
import com.deviceinsight.pro.domain.model.PerformanceSnapshot
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.utils.TimeWindows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMetricsRepositoryImpl @Inject constructor(
    private val dataSource: SystemMetricsDataSource,
    private val batteryDao: BatteryStatDao
) : DeviceMetricsRepository {

    override fun currentBattery(): BatteryInfo = dataSource.currentBattery()

    override fun currentPerformance(): PerformanceSnapshot = dataSource.currentPerformance()

    override fun observeBatteryHistory(days: Int): Flow<List<BatterySample>> =
        batteryDao.observeSince(TimeWindows.startOfDaysAgoMs(days - 1))
            .map { list -> list.map { it.toSample() } }

    override suspend fun sampleBattery() = withContext(Dispatchers.IO) {
        val b = dataSource.currentBattery()
        batteryDao.insert(
            BatteryStatEntity(
                level = b.level,
                isCharging = b.isCharging,
                temperatureC = b.temperatureC,
                voltageMv = b.voltageMv,
                health = b.health,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
