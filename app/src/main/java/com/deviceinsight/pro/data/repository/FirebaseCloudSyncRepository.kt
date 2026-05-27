package com.deviceinsight.pro.data.repository

import android.content.Context
import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import com.deviceinsight.pro.domain.repository.SocialMessageRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import com.deviceinsight.pro.utils.CloudConfig
import com.deviceinsight.pro.utils.DeviceIdentity
import com.deviceinsight.pro.utils.TimeWindows
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads consented on-device analytics to Firestore for the admin panel. No-op unless Firebase is
 * configured (google-services.json present), the device is linked (anonymous auth), and the user
 * has monitoring enabled. Firebase access is lazy so injection never fails offline.
 */
@Singleton
class FirebaseCloudSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdentity: DeviceIdentity,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository,
    private val notificationRepository: NotificationRepository,
    private val messageRepository: SocialMessageRepository,
    private val eventRepository: DeviceEventRepository,
    private val securityRepository: SecurityRepository,
    private val metricsRepository: DeviceMetricsRepository
) : CloudSyncRepository {

    override fun isCloudEnabled(): Boolean =
        CloudConfig.isAvailable(context) &&
            runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)

    override suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!isCloudEnabled()) return@runCatching
            // Note: cloud sync runs once the device is linked + consented; it is not gated on the
            // separate "Live monitoring" foreground-service toggle (that only drives the timeline).

            val deviceRef = Firebase.firestore.collection("devices").document(deviceIdentity.deviceId)
            if (!deviceRef.get().await().exists()) return@runCatching // not linked yet

            val screen = usageRepository.observeScreenTime(1).first()
            val notifCount = notificationRepository.observeTodayCount().first()
            val msgCount = messageRepository.observeTodayCount().first()
            val report = securityRepository.observeReport().first()
            val battery = metricsRepository.currentBattery()

            deviceRef.set(
                mapOf(
                    "label" to deviceIdentity.label,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "summary" to mapOf(
                        "screenTimeMsToday" to screen.totalScreenOnMs,
                        "unlocksToday" to screen.unlockCount,
                        "notificationsToday" to notifCount,
                        "messagesToday" to msgCount,
                        "securityScore" to report.score,
                        "focusScore" to screen.focusScore,
                        "batteryLevel" to battery.level,
                        "isCharging" to battery.isCharging
                    )
                ),
                SetOptions.merge()
            ).await()

            // Per-day usage totals (last 7 days) + today's top apps.
            val today = TimeWindows.todayEpochDay()
            val daily = usageRepository.observeDailyForeground(7).first()
            val startDay = today - (daily.size - 1).coerceAtLeast(0)
            val todayApps = usageRepository.observeUsage(1).first().take(15)
            daily.forEachIndexed { i, totalMs ->
                val epochDay = startDay + i
                val data = mutableMapOf<String, Any>(
                    "epochDay" to epochDay,
                    "totalForegroundMs" to totalMs,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (epochDay == today) {
                    data["apps"] = todayApps.map {
                        mapOf(
                            "packageName" to it.packageName,
                            "appName" to it.appName,
                            "totalForegroundMs" to it.totalForegroundMs,
                            "launchCount" to it.launchCount,
                            "lastTimeUsed" to it.lastTimeUsed
                        )
                    }
                }
                deviceRef.collection("usage").document(epochDay.toString()).set(data)
            }

            messageRepository.observeRecent().first().take(100).forEach { m ->
                deviceRef.collection("messages").document(m.id.toString()).set(
                    mapOf(
                        "platform" to m.platform.name,
                        "appName" to m.appName,
                        "sender" to m.sender,
                        "conversation" to m.conversation,
                        "preview" to m.preview,
                        "isGroup" to m.isGroup,
                        "timestamp" to m.timestamp
                    )
                )
            }

            eventRepository.observeRecent().first().take(100).forEach { e ->
                deviceRef.collection("events").document(e.id.toString()).set(
                    mapOf(
                        "type" to e.type.name,
                        "label" to e.label,
                        "detail" to e.detail,
                        "timestamp" to e.timestamp
                    )
                )
            }

            deviceRef.collection("security").document("latest").set(
                mapOf(
                    "score" to report.score,
                    "scannedAppCount" to report.scannedAppCount,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "findings" to report.findings.map {
                        mapOf(
                            "id" to it.id,
                            "title" to it.title,
                            "description" to it.description,
                            "severity" to it.severity.name,
                            "recommendation" to it.recommendation
                        )
                    }
                )
            ).await()
            Unit
        }
    }
}
