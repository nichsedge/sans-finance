package com.sans.finance.domain.usecase

import com.sans.finance.domain.repository.ExpenseRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


class GetFrequencyBasedSuggestionsUseCase constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): List<String> {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val dayOfWeek = today.dayOfWeek.ordinal // 0-6 starting from Monday usually, but let's check
        // DayOfWeek.MONDAY is 0 in Kotlin 1.9+, but in Calendar it's different.
        // The DAO expects strftime('%w', ...) which is 0=Sunday, 1=Monday, etc.
        val sundayBasedDay = (today.dayOfWeek.ordinal + 1) % 7
        
        val daySuggestions = repository.getTopFrequentTitlesByDay(sundayBasedDay, 5)
        if (daySuggestions.isNotEmpty()) return daySuggestions
        
        return repository.getTopFrequentTitles(5)
    }
}
