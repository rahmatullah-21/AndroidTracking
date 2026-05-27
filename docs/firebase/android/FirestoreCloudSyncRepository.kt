/*
 * DROP-IN REFERENCE — not compiled by default. Pairing-code model (see ../FIRESTORE_SCHEMA.md).
 *
 * To enable multi-device sync to the React admin panel:
 *   1. Add google-services.json to app/ and uncomment the google-services plugin + Firebase
 *      dependencies (firebase-auth, firebase-firestore) — see README "Firebase setup".
 *   2. Add: implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
 *   3. Enable Anonymous auth in the Firebase console.
 *   4. Copy this file + DeviceLinkRepository.kt + CloudSyncModule.kt + CloudSyncWorker.kt into the
 *      app, and REMOVE the NoOp `bindCloudSyncRepository` binding from RepositoryModule.kt.
 *   5. Add a "Cloud sync" settings screen (see CloudSyncScreen.kt) where the user enters the pairing
 *      code; then schedule CloudSyncWorker.
 *
 * The device authenticates ANONYMOUSLY and writes only the already-consented on-device analytics.
 * The device document is created at link time by DeviceLinkRepository; syncNow only refreshes it
 * (merge), so it never changes ownerUid/claimedByUid.
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
import com.google.firebase.firestore.SetOptions
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

    // Linked == signed in (anonymously) AND this device's doc has been claimed.
    override fun isCloudEnabled(): Boolean = auth.currentUser != null

    override suspend fun syncNow(): Result<Unit> = runCatching {
        auth.currentUser ?: error("Device not linked")
        if (!settingsRepository.observe().first().monitoringEnabled) return@runCatching

        val deviceRef = firestore.collection("devices").document(deviceIdentity.deviceId)
        // The doc must already exist (created by DeviceLinkRepository at link time).
        if (!deviceRef.get().await().exists()) error("Device not linked — claim a pairing code first")

        val screen = usageRepository.observeScreenTime(1).first()
        val notifCount = notificationRepository.observeTodayCount().first()
        val msgCount = messageRepository.observeTodayCount().first()
        val report = securityRepository.observeReport().first()
        val battery = metricsRepository.currentBattery()

        // Merge-update: refreshes summary + lastSeen without touching ownerUid/claimedByUid.
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

        // Messages (last 100)
        val msgCol = deviceRef.collection("messages")
        messageRepository.observeRecent().first().take(100).forEach { m ->
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
