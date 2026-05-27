package com.deviceinsight.pro.data.source

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.TrafficStats
import com.deviceinsight.pro.domain.model.NetworkSpeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/** Reads device-wide network totals and live throughput. */
@Singleton
class NetworkStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val nsm =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    data class Totals(val rxBytes: Long, val txBytes: Long)

    /** Device totals for a transport over a window. Requires Usage Access. */
    @Suppress("DEPRECATION") // NetworkStatsManager still keys on ConnectivityManager.TYPE_*
    fun deviceTotals(networkType: Int, begin: Long, end: Long): Totals = runCatching {
        val bucket = nsm.querySummaryForDevice(networkType, null, begin, end)
        Totals(bucket?.rxBytes ?: 0L, bucket?.txBytes ?: 0L)
    }.getOrElse { Totals(0L, 0L) }

    @Suppress("DEPRECATION")
    fun wifiTotals(begin: Long, end: Long): Totals =
        deviceTotals(ConnectivityManager.TYPE_WIFI, begin, end)

    @Suppress("DEPRECATION")
    fun mobileTotals(begin: Long, end: Long): Totals =
        deviceTotals(ConnectivityManager.TYPE_MOBILE, begin, end)

    /** Emits per-second throughput derived from cumulative [TrafficStats] counters. */
    fun realtimeSpeed(): Flow<NetworkSpeed> = flow {
        var lastRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
        var lastTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
        while (true) {
            delay(1000)
            val rx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
            val tx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
            emit(
                NetworkSpeed(
                    downloadBytesPerSec = (rx - lastRx).coerceAtLeast(0),
                    uploadBytesPerSec = (tx - lastTx).coerceAtLeast(0)
                )
            )
            lastRx = rx
            lastTx = tx
        }
    }
}
