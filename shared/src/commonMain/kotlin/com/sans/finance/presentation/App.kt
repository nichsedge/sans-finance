package com.sans.finance.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sans.finance.presentation.navigation.Screen
import com.sans.finance.presentation.main.MainScreen

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val rootNavController = rememberNavController()
            
            NavHost(
                navController = rootNavController,
                startDestination = Screen.Main
            ) {
                composable<Screen.Main> {
                    MainScreen(
                        rootNavController = rootNavController,
                        onLanguageToggle = { /* Handle language toggle */ }
                    )
                }
                // Other root-level screens can be added here
            }
        }
    }
}
