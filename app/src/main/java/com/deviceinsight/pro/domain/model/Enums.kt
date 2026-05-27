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
