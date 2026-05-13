package com.sans.finance.data.util

import com.sans.finance.domain.model.Expense
import com.sans.finance.core.util.DateTimeUtils

object CsvExporter {
    fun toCsv(expenses: List<Expense>): String {
        val header = "date,source,store,item_name,qty,item_price,order_total,status,is_installment"
        val csvLines = mutableListOf(header)

        expenses.forEach { expense ->
            val dateStr = DateTimeUtils.formatDate(expense.date) // This uses yyyy-MM-dd
            val source = escapeCsv(expense.tags.firstOrNull() ?: "")
            val details = escapeCsv(expense.details ?: "")
            val title = escapeCsv(expense.title)
            val qty = "1"

            // Re-calculate simple prices for export consistency with seeds
            val itemPrice = (expense.amount / 100.0).toString()
            val orderTotal = (expense.amount / 100.0).toString()
            val status = "Completed" // Default for sync
            val isInst = if (expense.isInstallment) "1" else "0"

            csvLines.add("$dateStr,$source,$details,$title,$qty,$itemPrice,$orderTotal,$status,$isInst")
        }

        return csvLines.joinToString("\n")
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
