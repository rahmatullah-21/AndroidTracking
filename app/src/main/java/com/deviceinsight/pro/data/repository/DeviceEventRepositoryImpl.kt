package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.database.dao.DeviceEventDao
import com.deviceinsight.pro.database.entity.DeviceEventEntity
import com.deviceinsight.pro.domain.model.DeviceEvent
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.utils.TimeWindows
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceEventRepositoryImpl @Inject constructor(
    private val dao: DeviceEventDao
) : DeviceEventRepository {

    override fun observeRecent(): Flow<List<DeviceEvent>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    override fun observeToday(): Flow<List<DeviceEvent>> =
        dao.observeSince(TimeWindows.startOfDaysAgoMs(0)).map { list -> list.map { it.toDomain() } }

    override suspend fun record(type: DeviceEventType, label: String, detail: String?) {
        dao.insert(
            DeviceEventEntity(
                type = type.name,
                label = label,
                detail = detail,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
