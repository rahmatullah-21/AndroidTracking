package com.deviceinsight.pro.utils

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Stable per-install device identifier + human-readable label, used for cloud sync. */
@Singleton
class DeviceIdentity @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("device_identity", Context.MODE_PRIVATE)

    val deviceId: String
        get() = prefs.getString(KEY_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_ID, it).apply()
        }

    val label: String = "${Build.MANUFACTURER} ${Build.MODEL}"

    private companion object {
        const val KEY_ID = "device_id"
    }
}
