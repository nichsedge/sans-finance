package com.sans.finance.di

import com.sans.finance.data.util.LocaleManager
import org.koin.core.module.Module
import org.koin.dsl.module
import com.russhwolf.settings.Settings

import androidx.room3.Room
import com.sans.finance.data.local.AppDatabase
import com.sans.finance.data.local.AppDatabaseConstructor
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import kotlin.js.JsAny

actual val platformModule: Module = module {
    single<Settings> { Settings() }
    single { LocaleManager(get()) }
    
    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(
            name = "sans_finance_web.db",
            factory = { AppDatabaseConstructor.initialize() }
        )
        .setDriver(WebWorkerSQLiteDriver(sqliteWorker))
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

private val sqliteWorker: JsAny =
    js("new Worker(new URL('sqlite-web-worker/worker.js', import.meta.url), { type: 'module' })")
