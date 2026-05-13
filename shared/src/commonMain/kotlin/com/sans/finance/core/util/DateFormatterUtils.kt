package com.sans.finance.core.util

import kotlinx.datetime.*

object DateFormatterUtils {
    fun formatStandard(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.dayOfMonth.toString().padStart(2, '0')} ${getMonthAbbr(dt.month)} ${dt.year}"
    }

    fun formatDayMonth(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.dayOfMonth} ${getMonthAbbr(dt.month)}"
    }

    fun formatMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${getMonthAbbr(dt.month)} ${dt.year.toString().takeLast(2)}"
    }

    fun formatYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return dt.year.toString()
    }

    private fun getMonthAbbr(month: Month): String {
        return month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }
}
