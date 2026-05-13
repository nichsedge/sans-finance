package com.sans.finance

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    org.koin.core.context.startKoin {
        modules(com.sans.finance.di.commonModule, com.sans.finance.di.platformModule)
    }
    ComposeViewport(document.body!!) {
        com.sans.finance.presentation.App()
    }
}
