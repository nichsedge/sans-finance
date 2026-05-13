package com.sans.finance.domain.usecase

import com.sans.finance.domain.model.Expense
import com.sans.finance.domain.repository.ExpenseRepository


class CheckDuplicateExpenseUseCase constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(title: String, amount: Long, date: Long, accountId: Long): Expense? {
        if (title.isBlank()) return null
        return repository.findPotentialDuplicate(title.trim(), amount, date, accountId)
    }
}
