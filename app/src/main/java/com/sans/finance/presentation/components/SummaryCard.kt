package com.sans.finance.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SummaryCard(
    income: Long,
    expense: Long,
    total: Long,
    currencyCode: String,
    avgMonthlyExpense: Long = 0L,
    isPrivacyModeEnabled: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    label = "Income",
                    amount = income,
                    currencyCode = currencyCode,
                    color = Color(0xFF4CAF50),
                    isPrivacyModeEnabled = isPrivacyModeEnabled,
                    modifier = Modifier.weight(1f)
                )
                
                SummaryItem(
                    label = "Expenses",
                    amount = expense,
                    currencyCode = currencyCode,
                    color = Color(0xFFE53935),
                    isPrivacyModeEnabled = isPrivacyModeEnabled,
                    modifier = Modifier.weight(1f)
                )

                SummaryItem(
                    label = "Total",
                    amount = total,
                    currencyCode = currencyCode,
                    color = MaterialTheme.colorScheme.onSurface,
                    isPrivacyModeEnabled = isPrivacyModeEnabled,
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                )
            }

            if (avgMonthlyExpense > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avg. Monthly Expense: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PrivacyText(
                        amount = avgMonthlyExpense,
                        currencyCode = currencyCode,
                        isVisible = !isPrivacyModeEnabled,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    amount: Long,
    currencyCode: String,
    color: Color,
    isPrivacyModeEnabled: Boolean,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        PrivacyText(
            amount = amount,
            currencyCode = currencyCode,
            isVisible = !isPrivacyModeEnabled,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = color,
            modifier = Modifier.fillMaxWidth(),
            textAlign = when (horizontalAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

