package com.deviceinsight.pro.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.deviceinsight.pro.database.entity.AppUsageEntity
import com.deviceinsight.pro.database.entity.BatteryStatEntity
import com.deviceinsight.pro.database.entity.DeviceEventEntity
import com.deviceinsight.pro.database.entity.NetworkUsageEntity
import com.deviceinsight.pro.database.entity.NotificationEntity
import com.deviceinsight.pro.database.entity.SecurityEventEntity
import com.deviceinsight.pro.database.entity.SocialMessageEntity
import com.deviceinsight.pro.database.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Upsert
    suspend fun upsertAll(rows: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE dateEpochDay = :day ORDER BY totalForegroundMs DESC")
    fun observeForDay(day: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE dateEpochDay BETWEEN :start AND :end")
    fun observeRange(start: Long, end: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :pkg AND dateEpochDay = :day LIMIT 1")
    suspend fun find(pkg: String, day: Long): AppUsageEntity?

    @Query("SELECT COALESCE(SUM(totalForegroundMs), 0) FROM app_usage WHERE dateEpochDay = :day")
    fun observeTotalForegroundForDay(day: Long): Flow<Long>

    @Query("DELETE FROM app_usage WHERE dateEpochDay < :beforeDay")
    suspend fun pruneBefore(beforeDay: Long)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 500): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :pkg ORDER BY postedAt DESC LIMIT :limit")
    fun observeForPackage(pkg: String, limit: Int = 500): Flow<List<NotificationEntity>>

    @Query(
        "SELECT * FROM notifications WHERE title LIKE '%' || :q || '%' " +
            "OR content LIKE '%' || :q || '%' OR appName LIKE '%' || :q || '%' " +
            "ORDER BY postedAt DESC LIMIT :limit"
    )
    fun search(q: String, limit: Int = 500): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE postedAt >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("SELECT appName, COUNT(*) AS cnt FROM notifications WHERE postedAt >= :since GROUP BY packageName ORDER BY cnt DESC LIMIT :limit")
    fun observeTopApps(since: Long, limit: Int = 10): Flow<List<AppCount>>

    @Query("DELETE FROM notifications WHERE postedAt < :before")
    suspend fun pruneBefore(before: Long)
}

data class AppCount(val appName: String, val cnt: Int)

@Dao
interface SocialMessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: SocialMessageEntity)

    @Query("SELECT * FROM social_messages ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 500): Flow<List<SocialMessageEntity>>

    @Query("SELECT * FROM social_messages WHERE platform = :platform ORDER BY timestamp DESC LIMIT :limit")
    fun observeForPlatform(platform: String, limit: Int = 500): Flow<List<SocialMessageEntity>>

    @Query(
        "SELECT * FROM social_messages WHERE sender LIKE '%' || :q || '%' " +
            "OR preview LIKE '%' || :q || '%' OR conversation LIKE '%' || :q || '%' " +
            "ORDER BY timestamp DESC LIMIT :limit"
    )
    fun search(q: String, limit: Int = 500): Flow<List<SocialMessageEntity>>

    @Query("SELECT platform AS platform, COUNT(*) AS cnt FROM social_messages WHERE timestamp >= :since GROUP BY platform ORDER BY cnt DESC")
    fun observePlatformCounts(since: Long): Flow<List<PlatformCount>>

    @Query("SELECT COUNT(*) FROM social_messages WHERE timestamp >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("DELETE FROM social_messages WHERE timestamp < :before")
    suspend fun pruneBefore(before: Long)
}

data class PlatformCount(val platform: String, val cnt: Int)

@Dao
interface DeviceEventDao {
    @Insert
    suspend fun insert(row: DeviceEventEntity)

    @Query("SELECT * FROM device_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 300): Flow<List<DeviceEventEntity>>

    @Query("SELECT * FROM device_events WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun observeSince(since: Long): Flow<List<DeviceEventEntity>>

    @Query("SELECT COUNT(*) FROM device_events WHERE type = :type AND timestamp >= :since")
    suspend fun countOfTypeSince(type: String, since: Long): Int

    @Query("DELETE FROM device_events WHERE timestamp < :before")
    suspend fun pruneBefore(before: Long)
}

@Dao
interface BatteryStatDao {
    @Insert
    suspend fun insert(row: BatteryStatEntity)

    @Query("SELECT * FROM battery_stats ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<BatteryStatEntity?>

    @Query("SELECT * FROM battery_stats WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun observeSince(since: Long): Flow<List<BatteryStatEntity>>

    @Query("DELETE FROM battery_stats WHERE timestamp < :before")
    suspend fun pruneBefore(before: Long)
}

@Dao
interface NetworkUsageDao {
    @Upsert
    suspend fun upsert(row: NetworkUsageEntity)

    @Query("SELECT * FROM network_usage WHERE dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay ASC")
    fun observeRange(start: Long, end: Long): Flow<List<NetworkUsageEntity>>

    @Query("SELECT * FROM network_usage WHERE dateEpochDay = :day LIMIT 1")
    suspend fun find(day: Long): NetworkUsageEntity?
}

@Dao
interface SecurityEventDao {
    @Query("DELETE FROM security_events")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<SecurityEventEntity>)

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SecurityEventEntity>>
}


@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 0 LIMIT 1")
    fun observe(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 0 LIMIT 1")
    suspend fun get(): UserSettingsEntity?

    @Upsert
    suspend fun upsert(row: UserSettingsEntity)
}
