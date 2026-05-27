package com.deviceinsight.pro.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.database.entity.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Captures posted notifications (title + short content preview only). Requires the user to
 * grant Notification Access; only stores metadata, never full message bodies or attachments.
 */
@AndroidEntryPoint
class AppNotificationListenerService : NotificationListenerService() {

    @Inject lateinit var notificationDao: NotificationDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg == packageName) return // ignore our own foreground notification

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val appName = loadAppName(pkg)
        val category = NotificationCategorizer.categorize(
            packageName = pkg,
            systemCategory = sbn.notification?.category,
            title = title,
            text = text
        )

        scope.launch {
            notificationDao.insert(
                NotificationEntity(
                    packageName = pkg,
                    appName = appName,
                    title = title.take(120),
                    content = text.take(200),
                    category = category.name,
                    postedAt = sbn.postTime
                )
            )
        }
    }

    private fun loadAppName(pkg: String): String = runCatching {
        val pm = packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
