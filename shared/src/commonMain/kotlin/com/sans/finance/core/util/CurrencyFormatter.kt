package com.sans.finance.core.util

import kotlin.math.abs
import kotlin.math.ceil

object CurrencyFormatter {
    fun formatAmount(amountInCents: Long, currencyCode: String = "USD"): String {
        val amount = ceil(amountInCents / 100.0).toLong()
        val symbol = when (currencyCode) {
            "IDR" -> "Rp"
            "USD" -> "$"
            "CNY" -> "¥"
            else -> currencyCode
        }
        
        val isNegative = amount < 0
        val absAmount = abs(amount)
        val formattedNumber = formatWithThousandSeparator(absAmount)
        
        val prefix = if (isNegative) "-" else ""
        return "$prefix$symbol$formattedNumber"
    }

    private fun formatWithThousandSeparator(number: Long): String {
        val s = number.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(",")
            }
        }
        return sb.reverse().toString()
    }

    fun formatAmountCompact(amountInCents: Long, currencyCode: String = "USD"): String {
        val amount = ceil(amountInCents / 100.0).toLong()
        val symbol = when (currencyCode) {
            "IDR" -> "Rp"
            "USD" -> "$"
            "CNY" -> "¥"
            else -> currencyCode
        }

        if (amount == 0L) return "${symbol}0"

        val isNegative = amount < 0
        val absAmount = abs(amount)
        val prefix = if (isNegative) "-$symbol" else symbol

        return when {
            absAmount >= 1_000_000_000L -> "$prefix${(absAmount / 1_000_000_000.0).toOneDecimalString()}B"
            absAmount >= 1_000_000L -> "$prefix${(absAmount / 1_000_000.0).toOneDecimalString()}M"
            absAmount >= 1_000L -> "$prefix${(absAmount / 1_000.0).toOneDecimalString()}K"
            else -> "$prefix$absAmount"
        }
    }

    private fun Double.toOneDecimalString(): String {
        val rounded = (this * 10).toLong() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }

    fun formatNumberOnly(amountInCents: Long): String {
        return ceil(amountInCents / 100.0).toLong().toString()
    }
}
