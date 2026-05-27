package com.deviceinsight.pro.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Periodically pushes consented analytics to the cloud. No-op unless linked + configured. */
@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val cloudSyncRepository: CloudSyncRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!cloudSyncRepository.isCloudEnabled()) return Result.success()
        return cloudSyncRepository.syncNow().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
