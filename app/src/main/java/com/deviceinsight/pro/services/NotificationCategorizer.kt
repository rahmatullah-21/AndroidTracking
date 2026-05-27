package com.deviceinsight.pro.services

import android.app.Notification
import com.deviceinsight.pro.domain.model.NotificationCategory

/** Maps a notification's system category + package heuristics to our coarse buckets. */
object NotificationCategorizer {

    private val socialPackages = listOf(
        "facebook", "instagram", "twitter", "snapchat", "tiktok", "reddit", "linkedin"
    )
    private val messagingPackages = listOf(
        "whatsapp", "telegram", "messenger", "messaging", "signal", "wechat", "sms", "mms"
    )

    fun categorize(packageName: String, systemCategory: String?, title: String, text: String): NotificationCategory {
        when (systemCategory) {
            Notification.CATEGORY_MESSAGE -> return NotificationCategory.MESSAGE
            Notification.CATEGORY_EMAIL -> return NotificationCategory.EMAIL
            Notification.CATEGORY_CALL -> return NotificationCategory.CALL
            Notification.CATEGORY_SOCIAL -> return NotificationCategory.SOCIAL
            Notification.CATEGORY_REMINDER, Notification.CATEGORY_EVENT,
            Notification.CATEGORY_ALARM -> return NotificationCategory.REMINDER
            Notification.CATEGORY_PROMO -> return NotificationCategory.PROMOTION
            Notification.CATEGORY_SYSTEM, Notification.CATEGORY_SERVICE,
            Notification.CATEGORY_PROGRESS -> return NotificationCategory.SYSTEM
        }

        val pkg = packageName.lowercase()
        return when {
            messagingPackages.any { pkg.contains(it) } -> NotificationCategory.MESSAGE
            socialPackages.any { pkg.contains(it) } -> NotificationCategory.SOCIAL
            pkg.contains("mail") || pkg.contains("gmail") || pkg.contains("outlook") ->
                NotificationCategory.EMAIL
            looksPromotional(title, text) -> NotificationCategory.PROMOTION
            else -> NotificationCategory.OTHER
        }
    }

    /** Lightweight heuristic flag for promotional/"spam" notifications. */
    fun looksPromotional(title: String, text: String): Boolean {
        val haystack = "$title $text".lowercase()
        val keywords = listOf(
            "sale", "% off", "discount", "offer", "deal", "coupon", "promo",
            "limited time", "buy now", "free", "win ", "cashback", "subscribe"
        )
        return keywords.any { haystack.contains(it) }
    }
}
