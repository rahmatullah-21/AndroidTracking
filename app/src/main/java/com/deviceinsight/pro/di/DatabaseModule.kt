package com.deviceinsight.pro.di

import android.content.Context
import androidx.room.Room
import com.deviceinsight.pro.database.AppDatabase
import com.deviceinsight.pro.database.dao.AppUsageDao
import com.deviceinsight.pro.database.dao.BatteryStatDao
import com.deviceinsight.pro.database.dao.DeviceEventDao
import com.deviceinsight.pro.database.dao.NetworkUsageDao
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.database.dao.SecurityEventDao
import com.deviceinsight.pro.database.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun appUsageDao(db: AppDatabase): AppUsageDao = db.appUsageDao()
    @Provides fun notificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
    @Provides fun deviceEventDao(db: AppDatabase): DeviceEventDao = db.deviceEventDao()
    @Provides fun batteryStatDao(db: AppDatabase): BatteryStatDao = db.batteryStatDao()
    @Provides fun networkUsageDao(db: AppDatabase): NetworkUsageDao = db.networkUsageDao()
    @Provides fun securityEventDao(db: AppDatabase): SecurityEventDao = db.securityEventDao()
    @Provides fun userSettingsDao(db: AppDatabase): UserSettingsDao = db.userSettingsDao()
}
