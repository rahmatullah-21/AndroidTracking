package com.deviceinsight.pro.data.repository

import android.content.Context
import com.deviceinsight.pro.utils.CloudConfig
import com.deviceinsight.pro.utils.DeviceIdentity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Links this device to an owner account via a pairing code (anonymous auth + Firestore claim).
 * All Firebase access is lazy and guarded, so this is safe to construct/inject even when Firebase
 * isn't configured (no google-services.json) — methods just report "not configured".
 */
@Singleton
class DeviceLinkRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdentity: DeviceIdentity
) {
    /** Firebase project present (google-services.json added at build time). */
    fun isAvailable(): Boolean = CloudConfig.isAvailable(context)

    /** True once this device has claimed itself (signed in anonymously). */
    fun isLinked(): Boolean = isAvailable() && runCatching { Firebase.auth.currentUser != null }
        .getOrDefault(false)

    suspend fun linkWithCode(pairingCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!isAvailable()) error("Cloud sync isn't configured. Add google-services.json and rebuild.")
            val code = pairingCode.trim().uppercase()
            require(code.isNotEmpty()) { "Enter the pairing code shown in the admin panel." }

            val auth = Firebase.auth
            val uid = (auth.currentUser ?: auth.signInAnonymously().await().user)?.uid
                ?: error("Could not sign in")

            val firestore = Firebase.firestore
            val codeSnap = firestore.collection("pairingCodes").document(code).get().await()
            if (!codeSnap.exists()) error("Invalid or expired code")
            val ownerUid = codeSnap.getString("ownerUid") ?: error("Invalid code")

            firestore.collection("devices").document(deviceIdentity.deviceId).set(
                mapOf(
                    "deviceId" to deviceIdentity.deviceId,
                    "label" to deviceIdentity.label,
                    "manufacturer" to deviceIdentity.manufacturer,
                    "model" to deviceIdentity.model,
                    "ownerUid" to ownerUid,
                    "claimedByUid" to uid,
                    "pairingCode" to code,
                    "monitoringConsent" to true,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "summary" to emptyMap<String, Any>()
                )
            ).await()
            Unit
        }
    }

    suspend fun unlink(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (!isAvailable()) return@runCatching
            runCatching {
                Firebase.firestore.collection("devices")
                    .document(deviceIdentity.deviceId).delete().await()
            }
            Firebase.auth.signOut()
        }
    }
}
