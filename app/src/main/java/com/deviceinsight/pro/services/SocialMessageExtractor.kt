package com.deviceinsight.pro.services

import android.app.Notification
import androidx.core.app.NotificationCompat

/**
 * Pulls a sender + message preview out of a posted notification. Prefers Android's
 * [NotificationCompat.MessagingStyle] (used by most chat apps) and falls back to the
 * notification title/text. This only ever sees the notification preview the OS exposes —
 * never the messaging app's private message store.
 */
object SocialMessageExtractor {

    data class Parsed(
        val sender: String,
        val conversation: String?,
        val preview: String,
        val isGroup: Boolean,
        val timestamp: Long
    )

    fun extract(notification: Notification, fallbackTitle: String, fallbackText: String): Parsed? {
        val style = runCatching {
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        }.getOrNull()

        if (style != null) {
            val last = style.messages.lastOrNull()
            val sender = last?.person?.name?.toString()
                ?: style.user.name?.toString()
                ?: fallbackTitle
            val text = last?.text?.toString() ?: fallbackText
            if (sender.isBlank() && text.isBlank()) return null
            return Parsed(
                sender = sender.ifBlank { fallbackTitle.ifBlank { "Unknown" } },
                conversation = style.conversationTitle?.toString(),
                preview = text,
                isGroup = style.isGroupConversation,
                timestamp = last?.timestamp ?: System.currentTimeMillis()
            )
        }

        if (fallbackTitle.isBlank() && fallbackText.isBlank()) return null
        return Parsed(
            sender = fallbackTitle.ifBlank { "Unknown" },
            conversation = null,
            preview = fallbackText,
            isGroup = false,
            timestamp = System.currentTimeMillis()
        )
    }
}
