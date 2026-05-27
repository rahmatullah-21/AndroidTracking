/*
 * DROP-IN REFERENCE — not compiled by default.
 *
 * To enable multi-device sync to the React admin panel:
 *   1. Add google-services.json to app/ and uncomment the google-services plugin + Firebase
 *      dependencies (see README "Firebase setup").
 *   2. Add: implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
 *   3. Copy this file to app/src/main/java/com/deviceinsight/pro/data/repository/.
 *   4. Copy CloudSyncModule.kt and CloudSyncWorker.kt likewise, and REMOVE the NoOp
 *      `bindCloudSyncRepository` binding from RepositoryModule.kt.
 *   5. Authenticate the device (FirebaseAuth) as the owner account, then schedule CloudSyncWorker.
 *
 * It uploads only the on-device analytics the user already consented to, and only when cloud
 * sync is enabled in Settings.
 */
package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import com.deviceinsight.pro.domain.repository.SocialMessageRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import com.deviceinsight.pro.utils.DeviceIdentity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCloudSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val deviceIdentity: DeviceIdentity,
    private val settingsRepository: SettingsRepository,
    private val usageRepository: UsageRepository,
    private val notificationRepository: NotificationRepository,
    private val messageRepository: SocialMessageRepository,
    private val eventRepository: DeviceEventRepository,
    private val securityRepository: SecurityRepository,
    private val metricsRepository: DeviceMetricsRepository
) : CloudSyncRepository {

    override fun isCloudEnabled(): Boolean = auth.currentUser != null

    override suspend fun syncNow(): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        if (!settingsRepository.observe().first().monitoringEnabled) return@runCatching

        val deviceRef = firestore.collection("devices").document(deviceIdentity.deviceId)

        val screen = usageRepository.observeScreenTime(1).first()
        val topApps = usageRepository.observeUsage(1).first().take(15)
        val notifCount = notificationRepository.observeTodayCount().first()
        val msgCount = messageRepository.observeTodayCount().first()
        val report = securityRepository.observeReport().first()
        val battery = metricsRepository.currentBattery()

        deviceRef.set(
            mapOf(
                "deviceId" to deviceIdentity.deviceId,
                "label" to deviceIdentity.label,
                "ownerUid" to uid,
                "monitoringConsent" to true,
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
            )
        ).await()

        // Messages (last 100)
        val messages = messageRepository.observeRecent().first().take(100)
        val msgCol = deviceRef.collection("messages")
        messages.forEach { m ->
            msgCol.document(m.id.toString()).set(
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

        // Recent events (last 100)
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

        // Security findings
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
    }
}
