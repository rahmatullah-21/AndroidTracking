package com.deviceinsight.pro.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deviceinsight.pro.database.dao.AppUsageDao
import com.deviceinsight.pro.database.dao.BatteryStatDao
import com.deviceinsight.pro.database.dao.DeviceEventDao
import com.deviceinsight.pro.database.dao.NetworkUsageDao
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.database.dao.SecurityEventDao
import com.deviceinsight.pro.database.dao.UserSettingsDao
import com.deviceinsight.pro.database.entity.AppUsageEntity
import com.deviceinsight.pro.database.entity.BatteryStatEntity
import com.deviceinsight.pro.database.entity.DeviceEventEntity
import com.deviceinsight.pro.database.entity.NetworkUsageEntity
import com.deviceinsight.pro.database.entity.NotificationEntity
import com.deviceinsight.pro.database.entity.SecurityEventEntity
import com.deviceinsight.pro.database.entity.UserSettingsEntity

@Database(
    entities = [
        AppUsageEntity::class,
        NotificationEntity::class,
        DeviceEventEntity::class,
        BatteryStatEntity::class,
        NetworkUsageEntity::class,
        SecurityEventEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun deviceEventDao(): DeviceEventDao
    abstract fun batteryStatDao(): BatteryStatDao
    abstract fun networkUsageDao(): NetworkUsageDao
    abstract fun securityEventDao(): SecurityEventDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val NAME = "device_insight.db"
    }
}
