package com.deviceinsight.pro.utils

object Constants {
    const val MONITORING_CHANNEL_ID = "monitoring_channel"
    const val MONITORING_NOTIFICATION_ID = 1001
    const val ALERTS_CHANNEL_ID = "alerts_channel"

    const val USAGE_SYNC_WORK = "usage_sync_work"
    const val DAILY_REPORT_WORK = "daily_report_work"

    /** How long monitoring data is retained on-device before pruning. */
    const val DATA_RETENTION_DAYS = 90L

    /** Battery percentage threshold for a "battery low" timeline event. */
    const val BATTERY_LOW_THRESHOLD = 15
}
