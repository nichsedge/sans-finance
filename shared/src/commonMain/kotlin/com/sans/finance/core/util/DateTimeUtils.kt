package com.sans.finance.core.util

import kotlinx.datetime.*

object DateTimeUtils {
    fun getNow(): Long = Clock.System.now().toEpochMilliseconds()

    fun getStartOfDay(timestamp: Long): Long {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return LocalDateTime(dateTime.year, dateTime.month, dateTime.dayOfMonth, 0, 0, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    fun getStartOfMonth(timestamp: Long): Long {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return LocalDateTime(dateTime.year, dateTime.month, 1, 0, 0, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    fun getStartOfYear(timestamp: Long): Long {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return LocalDateTime(dateTime.year, Month.JANUARY, 1, 0, 0, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    fun addMonths(timestamp: Long, months: Int): Long {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        
        var year = dateTime.year
        var month = dateTime.monthNumber + months
        
        while (month > 12) {
            month -= 12
            year += 1
        }
        while (month < 1) {
            month += 12
            year -= 1
        }
        
        val daysInMonth = getDaysInMonth(year, month)
        val day = dateTime.dayOfMonth.coerceAtMost(daysInMonth)
        
        return LocalDateTime(year, Month(month), day, dateTime.hour, dateTime.minute, dateTime.second, dateTime.nanosecond)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    fun addDays(timestamp: Long, days: Int): Long {
        return timestamp + (days.toLong() * 24 * 60 * 60 * 1000L)
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
            else -> 30
        }
    }

    fun formatDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth} ${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} ${dateTime.year}"
    }

    fun formatMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.year}"
    }

    fun formatYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return dateTime.year.toString()
    }

    fun getDayOfMonth(timestamp: Long): Int {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
    }

    fun getDayOfWeekName(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val day = instant.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek
        return day.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun formatFullDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth} ${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.year}"
    }
}
