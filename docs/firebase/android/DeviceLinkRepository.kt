/*
 * DROP-IN REFERENCE — not compiled by default. Pairing-code device linking (see ../FIRESTORE_SCHEMA.md).
 *
 * Copy to app/src/main/java/com/deviceinsight/pro/data/repository/ once Firebase is enabled.
 * Handles the one-time "claim": the device signs in anonymously and registers itself under the
 * owner identified by a pairing code generated in the admin panel. Firestore rules verify the code
 * server-side, so a device can never register under an owner without a valid, unexpired code.
 */
package com.deviceinsight.pro.data.repository

import com.deviceinsight.pro.utils.DeviceIdentity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceLinkRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val deviceIdentity: DeviceIdentity
) {
    val isLinked: Boolean get() = auth.currentUser != null

    /** Claims this device under the owner who issued [pairingCode]. */
    suspend fun linkWithCode(pairingCode: String): Result<Unit> = runCatching {
        val code = pairingCode.trim().uppercase()
        require(code.isNotEmpty()) { "Enter the pairing code shown in the admin panel" }

        // 1. Anonymous sign-in gives this device a uid (used as claimedByUid).
        val uid = (auth.currentUser ?: auth.signInAnonymously().await().user)?.uid
            ?: error("Could not sign in")

        // 2. Resolve the owner from the pairing code (readable by exact id only).
        val codeSnap = firestore.collection("pairingCodes").document(code).get().await()
        if (!codeSnap.exists()) error("Invalid or expired code")
        val ownerUid = codeSnap.getString("ownerUid") ?: error("Invalid code")

        // 3. Create this device's document. Security rules re-validate the code + expiry.
        firestore.collection("devices").document(deviceIdentity.deviceId).set(
            mapOf(
                "deviceId" to deviceIdentity.deviceId,
                "label" to deviceIdentity.label,
                "ownerUid" to ownerUid,
                "claimedByUid" to uid,
                "pairingCode" to code,
                "monitoringConsent" to true,
                "lastSeen" to FieldValue.serverTimestamp(),
                "summary" to emptyMap<String, Any>()
            )
        ).await()
    }

    /** Unlink this device (stops it appearing in the panel on next owner refresh). */
    suspend fun unlink() {
        runCatching {
            firestore.collection("devices").document(deviceIdentity.deviceId).delete().await()
        }
        auth.signOut()
    }
}
