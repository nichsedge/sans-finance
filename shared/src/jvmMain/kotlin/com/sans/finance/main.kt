package com.sans.finance

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sans.finance.presentation.App
import com.sans.finance.di.commonModule
import com.sans.finance.di.platformModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(commonModule, platformModule)
    }
    Window(onCloseRequest = ::exitApplication, title = "Sans Finance") {
        App()
    }
}
