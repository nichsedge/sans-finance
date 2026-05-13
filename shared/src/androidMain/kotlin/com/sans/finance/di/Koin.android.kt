package com.sans.finance.di

import androidx.room3.Room
import com.sans.finance.data.local.AppDatabase
import com.sans.finance.data.util.LocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module
import android.content.Context

actual val platformModule: Module = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "sans_finance_db"
        )
            .addMigrations(*com.sans.finance.data.local.Migrations.ALL_MIGRATIONS)
            .build()
    }

    single { get<AppDatabase>().expenseDao }
    single { get<AppDatabase>().categoryDao }
    single { get<AppDatabase>().installmentDao }
    single { get<AppDatabase>().tagDao }
    single { get<AppDatabase>().accountDao }
    single { get<AppDatabase>().goalDao }
    single { get<AppDatabase>().budgetDao }
    single { get<AppDatabase>().portfolioDao }
    single { get<AppDatabase>().currencyDao }

    single<com.russhwolf.settings.Settings> {
        com.russhwolf.settings.SharedPreferencesSettings(
            get<android.content.Context>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        )
    }

    single { LocaleManager(get()) }
}
