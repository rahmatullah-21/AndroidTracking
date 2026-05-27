package com.deviceinsight.pro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.deviceinsight.pro.utils.Constants
import com.deviceinsight.pro.workers.CloudSyncWorker
import com.deviceinsight.pro.workers.DailyMaintenanceWorker
import com.deviceinsight.pro.workers.UsageSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DeviceInsightApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleBackgroundWork()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                Constants.MONITORING_CHANNEL_ID,
                getString(R.string.monitoring_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.monitoring_channel_desc) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                Constants.ALERTS_CHANNEL_ID,
                "Alerts & Limits",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun scheduleBackgroundWork() {
        val wm = WorkManager.getInstance(this)

        val usageSync = PeriodicWorkRequestBuilder<UsageSyncWorker>(1, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(
            Constants.USAGE_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            usageSync
        )

        val maintenance = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork(
            Constants.DAILY_REPORT_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            maintenance
        )

        // Cloud sync (no-op unless the device is linked + Firebase configured).
        val cloudSync = PeriodicWorkRequestBuilder<CloudSyncWorker>(2, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(
            Constants.CLOUD_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            cloudSync
        )
    }
}
