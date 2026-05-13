package com.sans.finance.di

import com.sans.finance.data.util.LocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module
import com.russhwolf.settings.Settings

import androidx.room3.Room
import com.sans.finance.data.local.AppDatabase
import com.sans.finance.data.local.AppDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual val platformModule: Module = module {
    single<Settings> { com.russhwolf.settings.Settings() }
    single { LocaleManager(get()) }
    
    single<AppDatabase> {
        val dbFile = File(System.getProperty("user.home"), ".sans_finance/sans_finance.db")
        if (!dbFile.parentFile.exists()) dbFile.parentFile.mkdirs()
        
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = { AppDatabaseConstructor.initialize() }
        )
        .addMigrations(*com.sans.finance.data.local.Migrations.ALL_MIGRATIONS)
        .setDriver(BundledSQLiteDriver())
        .build()
    }

    single { get<AppDatabase>().expenseDao }
    single { get<AppDatabase>().categoryDao }
    single { get<AppDatabase>().installmentDao }
    single { get<AppDatabase>().tagDao }
    single { get<AppDatabase>().accountDao }
    single { get<AppDatabase>().goalDao }
    single { get<AppDatabase>().budgetDao }
    single { get<AppDatabase>().currencyDao }
    single { get<AppDatabase>().portfolioDao }
}
