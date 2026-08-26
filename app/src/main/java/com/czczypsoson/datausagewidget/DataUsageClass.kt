package com.czczypsoson.datausagewidget

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import java.util.Calendar
import java.util.Locale

class DataUsageManager(private val context: Context) {

    fun getMonthlyMobileDataUsage(): Long {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        // Nastavení času: Od 1. dne aktuálního měsíce 00:00:00 do teď
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        var totalBytes = 0L

        try {
            // Dotaz na mobilní data (TYPE_MOBILE)
            val bucket = networkStatsManager.querySummaryForDevice(
                NetworkCapabilities.TRANSPORT_CELLULAR,
                null,
                startTime,
                endTime
            )
            totalBytes = bucket.rxBytes + bucket.txBytes // Prijatá + odoslaná data
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return totalBytes
    }

    // Pomocná funkce pro převedení Bytů na MB / GB
    fun formatDataSize(bytes: Long): String {
        val megabytes = bytes / 1000000
        return if (megabytes >= 10000) {
            String.format(Locale.US, "%.2f GB", megabytes / 1000.0)
        } else if (megabytes >= 1000) {
            String.format(Locale.US, "%.3f GB", megabytes / 1000.0)
        } else {
            String.format(Locale.US, "%.1f MB", megabytes)
        }
    }
}