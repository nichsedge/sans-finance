package com.sans.finance.domain.usecase

import com.sans.finance.domain.repository.ExpenseRepository


class GetTitleSuggestionsUseCase constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(query: String): List<String> {
        return repository.getTitleSuggestions(query)
    }
}
