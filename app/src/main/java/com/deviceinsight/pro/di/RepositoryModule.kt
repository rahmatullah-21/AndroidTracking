package com.deviceinsight.pro.di

import com.deviceinsight.pro.data.repository.DeviceEventRepositoryImpl
import com.deviceinsight.pro.data.repository.DeviceMetricsRepositoryImpl
import com.deviceinsight.pro.data.repository.NetworkRepositoryImpl
import com.deviceinsight.pro.data.repository.NoOpCloudSyncRepository
import com.deviceinsight.pro.data.repository.NotificationRepositoryImpl
import com.deviceinsight.pro.data.repository.SecurityRepositoryImpl
import com.deviceinsight.pro.data.repository.SettingsRepositoryImpl
import com.deviceinsight.pro.data.repository.SocialMessageRepositoryImpl
import com.deviceinsight.pro.data.repository.UsageRepositoryImpl
import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import com.deviceinsight.pro.domain.repository.SocialMessageRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindDeviceEventRepository(impl: DeviceEventRepositoryImpl): DeviceEventRepository

    @Binds @Singleton
    abstract fun bindDeviceMetricsRepository(impl: DeviceMetricsRepositoryImpl): DeviceMetricsRepository

    @Binds @Singleton
    abstract fun bindNetworkRepository(impl: NetworkRepositoryImpl): NetworkRepository

    @Binds @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindSocialMessageRepository(impl: SocialMessageRepositoryImpl): SocialMessageRepository

    // Default no-op cloud sync. Swap for a Firestore implementation to enable the admin panel.
    @Binds @Singleton
    abstract fun bindCloudSyncRepository(impl: NoOpCloudSyncRepository): CloudSyncRepository
}
