package com.deviceinsight.pro.di

import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Lets [android.content.BroadcastReceiver]s pull singletons from the Hilt graph without the
 * abstract-`onReceive` pitfalls of `@AndroidEntryPoint` on receivers.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReceiverEntryPoint {
    fun deviceEventRepository(): DeviceEventRepository
    fun settingsRepository(): SettingsRepository
}
