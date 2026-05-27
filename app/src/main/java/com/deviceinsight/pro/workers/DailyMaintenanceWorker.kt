package com.deviceinsight.pro.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deviceinsight.pro.database.dao.AppUsageDao
import com.deviceinsight.pro.database.dao.BatteryStatDao
import com.deviceinsight.pro.database.dao.DeviceEventDao
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.utils.Constants
import com.deviceinsight.pro.utils.TimeWindows
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Runs a daily security scan and prunes data older than the retention window. */
@HiltWorker
class DailyMaintenanceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val securityRepository: SecurityRepository,
    private val appUsageDao: AppUsageDao,
    private val notificationDao: NotificationDao,
    private val deviceEventDao: DeviceEventDao,
    private val batteryStatDao: BatteryStatDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        runCatching { securityRepository.runScan() }

        val cutoffDay = TimeWindows.todayEpochDay() - Constants.DATA_RETENTION_DAYS
        val cutoffMs = TimeWindows.startOfDayMs(cutoffDay)
        runCatching {
            appUsageDao.pruneBefore(cutoffDay)
            notificationDao.pruneBefore(cutoffMs)
            deviceEventDao.pruneBefore(cutoffMs)
            batteryStatDao.pruneBefore(cutoffMs)
        }
        return Result.success()
    }
}
