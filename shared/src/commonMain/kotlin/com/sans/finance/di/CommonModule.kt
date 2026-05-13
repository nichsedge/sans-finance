package com.sans.finance.di

import com.sans.finance.domain.repository.*
import com.sans.finance.data.repository.*
import com.sans.finance.domain.usecase.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule = module {
    // Repositories
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<ExpenseRepository> { ExpenseRepositoryImpl(get(), get(), get(), get(), get(), get()) }
    single<GoalRepository> { GoalRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<PortfolioRepository> { PortfolioRepositoryImpl(get()) }
    single<CurrencyRepository> { CurrencyRepositoryImpl(get()) }
    single<InstallmentRepository> { InstallmentRepositoryImpl(get()) }

    // Use Cases
    factory { GetExpensesUseCase(get()) }
    factory { AddTransactionUseCase(get()) }
    factory { UpdateExpenseUseCase(get(), get(), get()) }
    factory { DeleteExpenseUseCase(get(), get()) }
    factory { GetExpenseByIdUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { GetTitleSuggestionsUseCase(get()) }
    factory { GetDetailsSuggestionsUseCase(get()) }
    factory { ConvertCurrencyUseCase(get()) }
    factory { CheckDuplicateExpenseUseCase(get()) }
    factory { PredictTransactionUseCase(get()) }
    factory { CreateInstallmentPlanUseCase(get()) }
    factory { GetFrequencyBasedSuggestionsUseCase(get()) }
    // ViewModels
    viewModel { com.sans.finance.presentation.dashboard.DashboardViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { com.sans.finance.presentation.expense_list.ExpenseListViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { com.sans.finance.presentation.add_transaction.AddTransactionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { com.sans.finance.presentation.portfolio.PortfolioViewModel(get(), get()) }
    viewModel { com.sans.finance.presentation.accounts.AccountViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.accounts.AccountStatsViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.budgeting.BudgetViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.goals.GoalViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.settings.SettingsViewModel(get(), get(), get(), get()) }
    viewModel { com.sans.finance.presentation.settings.categories.CategorySettingsViewModel(get()) }
    viewModel { com.sans.finance.presentation.settings.tags.TagSettingsViewModel(get()) }
    viewModel { com.sans.finance.presentation.installments.InstallmentsViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.forecasting.WealthForecastingViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.recurring.RecurringExpensesViewModel(get(), get()) }
    viewModel { com.sans.finance.presentation.transaction_stats.TransactionStatsViewModel(get(), get(), get()) }
    viewModel { com.sans.finance.presentation.search.SearchViewModel(get(), get(), get(), get()) }
}
