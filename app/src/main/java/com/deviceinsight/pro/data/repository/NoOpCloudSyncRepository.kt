package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default cloud-sync binding: does nothing. Keeps the app fully functional and offline-only
 * out of the box. Replace this binding with a real Firestore implementation to enable the
 * multi-device admin panel (see docs/firebase).
 */
@Singleton
class NoOpCloudSyncRepository @Inject constructor() : CloudSyncRepository {
    override fun isCloudEnabled(): Boolean = false
    override suspend fun syncNow(): Result<Unit> = Result.success(Unit)
}
