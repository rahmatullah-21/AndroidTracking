package com.deviceinsight.pro.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deviceinsight.pro.database.dao.NotificationDao
import com.deviceinsight.pro.database.dao.SocialMessageDao
import com.deviceinsight.pro.database.entity.NotificationEntity
import com.deviceinsight.pro.database.entity.SocialMessageEntity
import com.deviceinsight.pro.domain.model.SocialPlatform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Captures posted notifications (title + short content preview only) and, for recognized
 * social/messaging apps, the sender + message preview for the Messages monitor.
 *
 * Requires the user to grant Notification Access. It stores only notification metadata/previews,
 * never full message bodies, attachments, or the chat app's private database. The monitored device
 * shows this access in system settings — use only on devices you own or are authorized to monitor.
 */
@AndroidEntryPoint
class AppNotificationListenerService : NotificationListenerService() {

    @Inject lateinit var notificationDao: NotificationDao
    @Inject lateinit var socialMessageDao: SocialMessageDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg == packageName) return // ignore our own foreground notification

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val appName = loadAppName(pkg)
        val category = NotificationCategorizer.categorize(
            packageName = pkg,
            systemCategory = notification.category,
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

        // Social / messaging apps → capture the message preview for the Messages monitor.
        if (SocialPlatform.isMessagingApp(pkg)) {
            val parsed = SocialMessageExtractor.extract(notification, title, text) ?: return
            val platform = SocialPlatform.fromPackage(pkg)
            scope.launch {
                socialMessageDao.insert(
                    SocialMessageEntity(
                        platform = platform.name,
                        packageName = pkg,
                        appName = appName,
                        sender = parsed.sender.take(80),
                        conversation = parsed.conversation?.take(80),
                        preview = parsed.preview.take(300),
                        isGroup = parsed.isGroup,
                        timestamp = parsed.timestamp
                    )
                )
            }
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
