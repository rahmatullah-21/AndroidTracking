package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.data.mapper.toDomain
import com.deviceinsight.pro.data.mapper.toEntity
import com.deviceinsight.pro.database.dao.UserSettingsDao
import com.deviceinsight.pro.domain.model.WellbeingSettings
import com.deviceinsight.pro.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dao: UserSettingsDao
) : SettingsRepository {

    override fun observe(): Flow<WellbeingSettings> =
        dao.observe().map { it?.toDomain() ?: WellbeingSettings() }

    override suspend fun update(transform: (WellbeingSettings) -> WellbeingSettings) =
        withContext(Dispatchers.IO) {
            val current = dao.get()?.toDomain() ?: WellbeingSettings()
            dao.upsert(transform(current).toEntity())
        }
}
