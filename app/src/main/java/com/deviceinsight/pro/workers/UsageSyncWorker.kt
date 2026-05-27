package com.deviceinsight.pro.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Periodically pulls usage, network and battery data into the local database. */
@HiltWorker
class UsageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val usageRepository: UsageRepository,
    private val networkRepository: NetworkRepository,
    private val deviceMetricsRepository: DeviceMetricsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!usageRepository.hasUsageAccess()) return Result.success()
        usageRepository.syncUsage()
        networkRepository.syncToday()
        runCatching { deviceMetricsRepository.sampleBattery() }
        return Result.success()
    }
}
