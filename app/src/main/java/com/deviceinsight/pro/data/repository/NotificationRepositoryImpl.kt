package com.deviceinsight.pro.data.repository

import android.content.Context
import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.domain.model.NotificationInfo
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.utils.PermissionUtils
import com.deviceinsight.pro.utils.TimeWindows
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: NotificationDao
) : NotificationRepository {

    override fun observeRecent(): Flow<List<NotificationInfo>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    override fun observeForApp(packageName: String): Flow<List<NotificationInfo>> =
        dao.observeForPackage(packageName).map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<NotificationInfo>> =
        if (query.isBlank()) observeRecent()
        else dao.search(query.trim()).map { list -> list.map { it.toDomain() } }

    override fun observeTodayCount(): Flow<Int> =
        dao.observeCountSince(TimeWindows.startOfDaysAgoMs(0))

    override fun observeTopApps(): Flow<List<Pair<String, Int>>> =
        dao.observeTopApps(TimeWindows.startOfDaysAgoMs(0))
            .map { list -> list.map { it.appName to it.cnt } }

    override fun hasNotificationAccess(): Boolean =
        PermissionUtils.hasNotificationAccess(context)
}
