package com.sans.finance.core.util

import kotlin.math.abs
import kotlin.math.round

object NumberFormatUtils {
    fun formatFixed(value: Double, decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = round(value * factor) / factor
        val sign = if (rounded < 0) "-" else ""
        val absValue = abs(rounded)
        val whole = absValue.toLong()
        val fraction = ((absValue - whole) * factor).toLong().toString().padStart(decimals, '0')
        return if (decimals == 0) "$sign$whole" else "$sign$whole.$fraction"
    }

    fun formatGrouped(value: Double, decimals: Int): String {
        val fixed = formatFixed(value, decimals)
        val negative = fixed.startsWith('-')
        val unsigned = if (negative) fixed.drop(1) else fixed
        val parts = unsigned.split('.')
        val groupedWhole = withGrouping(parts[0])
        val result = if (parts.size > 1) "$groupedWhole.${parts[1]}" else groupedWhole
        return if (negative) "-$result" else result
    }

    fun formatPercent(value: Double, decimals: Int): String = "${formatFixed(value, decimals)}%"

    private fun withGrouping(number: String): String {
        val sb = StringBuilder()
        var count = 0
        for (i in number.length - 1 downTo 0) {
            sb.append(number[i])
            count++
            if (count % 3 == 0 && i > 0) sb.append(',')
        }
        return sb.reverse().toString()
    }

    private fun pow10(decimals: Int): Double {
        var result = 1.0
        repeat(decimals) { result *= 10.0 }
        return result
    }
}
