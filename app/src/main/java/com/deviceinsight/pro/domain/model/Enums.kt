package com.deviceinsight.pro.domain.model

/** Types of events recorded on the device-activity timeline. */
enum class DeviceEventType {
    APP_OPENED,
    APP_CLOSED,
    DEVICE_UNLOCKED,
    DEVICE_LOCKED,
    SCREEN_ON,
    SCREEN_OFF,
    CHARGING_CONNECTED,
    CHARGING_DISCONNECTED,
    WIFI_ENABLED,
    WIFI_DISABLED,
    BLUETOOTH_CONNECTED,
    BLUETOOTH_DISCONNECTED,
    BATTERY_LOW,
    HEADPHONES_CONNECTED,
    HEADPHONES_DISCONNECTED,
    BOOT_COMPLETED,
    UNKNOWN
}

/** Coarse notification category for analytics and spam detection. */
enum class NotificationCategory {
    MESSAGE,
    EMAIL,
    SOCIAL,
    CALL,
    REMINDER,
    PROMOTION,
    SYSTEM,
    OTHER
}

/** Severity buckets for the security center. */
enum class SecuritySeverity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

/** Social / messaging platforms recognized by the message monitor. */
enum class SocialPlatform(val displayName: String, private val matchers: List<String>) {
    WHATSAPP("WhatsApp", listOf("com.whatsapp")),
    MESSENGER("Messenger", listOf("com.facebook.orca", "com.facebook.mlite")),
    INSTAGRAM("Instagram", listOf("com.instagram.android")),
    TELEGRAM("Telegram", listOf("org.telegram", "telegram")),
    SNAPCHAT("Snapchat", listOf("com.snapchat.android")),
    SIGNAL("Signal", listOf("org.thoughtcrime.securesms", "signal")),
    DISCORD("Discord", listOf("com.discord")),
    WECHAT("WeChat", listOf("com.tencent.mm")),
    TWITTER("X / Twitter", listOf("com.twitter.android")),
    SMS("SMS / Messages", listOf("messaging", "com.android.mms", ".sms", ".mms")),
    OTHER("Other", emptyList());

    companion object {
        fun fromPackage(packageName: String): SocialPlatform {
            val pkg = packageName.lowercase()
            return entries.firstOrNull { p -> p.matchers.any { pkg.contains(it) } } ?: OTHER
        }

        /** True if the package is a recognized social/messaging app. */
        fun isMessagingApp(packageName: String): Boolean = fromPackage(packageName) != OTHER
    }
}

/** Sort orders available on the App Usage screen. */
enum class UsageSortOrder(val label: String) {
    MOST_USED("Most used"),
    LEAST_USED("Least used"),
    RECENTLY_OPENED("Recently opened"),
    MOST_LAUNCHED("Most launched")
}

/** Range selector shared by analytics screens. */
enum class TimeRange(val label: String, val days: Int) {
    TODAY("Today", 1),
    WEEK("This week", 7),
    MONTH("This month", 30)
}

/** Theme preference. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
