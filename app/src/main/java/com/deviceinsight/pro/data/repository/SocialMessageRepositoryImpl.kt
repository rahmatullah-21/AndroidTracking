package com.deviceinsight.pro.data.repository

import android.content.Context
import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.database.dao.SocialMessageDao
import com.deviceinsight.pro.domain.model.SocialMessage
import com.deviceinsight.pro.domain.model.SocialPlatform
import com.deviceinsight.pro.domain.repository.SocialMessageRepository
import com.deviceinsight.pro.utils.PermissionUtils
import com.deviceinsight.pro.utils.TimeWindows
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialMessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: SocialMessageDao
) : SocialMessageRepository {

    override fun observeRecent(): Flow<List<SocialMessage>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    override fun observeForPlatform(platform: SocialPlatform): Flow<List<SocialMessage>> =
        dao.observeForPlatform(platform.name).map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<SocialMessage>> =
        if (query.isBlank()) observeRecent()
        else dao.search(query.trim()).map { list -> list.map { it.toDomain() } }

    override fun observeTodayCount(): Flow<Int> =
        dao.observeCountSince(TimeWindows.startOfDaysAgoMs(0))

    override fun observePlatformCounts(): Flow<List<Pair<SocialPlatform, Int>>> =
        dao.observePlatformCounts(TimeWindows.startOfDaysAgoMs(0)).map { list ->
            list.map { pc ->
                val platform = runCatching { SocialPlatform.valueOf(pc.platform) }
                    .getOrDefault(SocialPlatform.OTHER)
                platform to pc.cnt
            }
        }

    override fun hasNotificationAccess(): Boolean =
        PermissionUtils.hasNotificationAccess(context)
}
