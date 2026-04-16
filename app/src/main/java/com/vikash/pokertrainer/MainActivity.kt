package com.vikash.pokertrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vikash.pokertrainer.ui.navigation.PokerNavGraph
import com.vikash.pokertrainer.ui.theme.PokerTrainerTheme
import com.vikash.pokertrainer.ui.theme.pokerColors
import com.vikash.pokertrainer.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            PokerTrainerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = pokerColors.background
                ) {
                    val navController = rememberNavController()
                    PokerNavGraph(
                        navController = navController,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}
