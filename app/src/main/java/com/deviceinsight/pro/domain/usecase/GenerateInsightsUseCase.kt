package com.deviceinsight.pro.domain.usecase

import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.utils.Formatters
import javax.inject.Inject

/** Derives short productivity/wellbeing insights from raw stats. */
class GenerateInsightsUseCase @Inject constructor() {

    operator fun invoke(
        screenTime: ScreenTimeStats,
        topApps: List<AppUsageInfo>,
        dailyGoalMinutes: Int
    ): List<String> {
        val insights = mutableListOf<String>()

        val goalMs = dailyGoalMinutes * 60_000L
        if (goalMs > 0) {
            if (screenTime.totalScreenOnMs > goalMs) {
                insights += "You're ${Formatters.duration(screenTime.totalScreenOnMs - goalMs)} over " +
                    "your daily screen-time goal."
            } else {
                insights += "Nice — you're within your daily screen-time goal " +
                    "(${Formatters.duration(goalMs - screenTime.totalScreenOnMs)} to spare)."
            }
        }

        topApps.firstOrNull()?.let { top ->
            insights += "${top.appName} is your most-used app today " +
                "(${Formatters.duration(top.totalForegroundMs)})."
        }

        if (screenTime.unlockCount > 0) {
            insights += "You unlocked your device ${screenTime.unlockCount} times today."
        }

        if (screenTime.peakHour in 0..23) {
            val end = (screenTime.peakHour + 1) % 24
            insights += "Peak usage was between %02d:00 and %02d:00.".format(screenTime.peakHour, end)
        }

        insights += "Focus score: ${screenTime.focusScore}/100."
        return insights
    }
}
