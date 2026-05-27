package com.deviceinsight.pro.utils

import android.content.Context
import com.google.firebase.FirebaseApp

/** Whether a Firebase project is configured (i.e. google-services.json was added at build time). */
object CloudConfig {
    fun isAvailable(context: Context): Boolean = runCatching {
        FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)
}
