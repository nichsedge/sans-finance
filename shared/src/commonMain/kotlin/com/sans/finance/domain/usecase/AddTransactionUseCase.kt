package com.sans.finance.domain.usecase

import com.sans.finance.domain.model.Expense
import com.sans.finance.domain.repository.ExpenseRepository


class AddTransactionUseCase constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Long {
        return repository.insertExpense(expense)
    }
}
