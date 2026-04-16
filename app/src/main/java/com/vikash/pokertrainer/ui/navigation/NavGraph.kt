package com.vikash.pokertrainer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vikash.pokertrainer.ui.screens.gto.GtoChartScreen
import com.vikash.pokertrainer.ui.screens.home.HomeScreen
import com.vikash.pokertrainer.ui.screens.playertype.PlayerTypeScreen
import com.vikash.pokertrainer.ui.screens.scenario.ScenarioScreen
import com.vikash.pokertrainer.ui.screens.stats.StatsScreen
import com.vikash.pokertrainer.viewmodel.GtoViewModel
import com.vikash.pokertrainer.viewmodel.PlayerTypeViewModel
import com.vikash.pokertrainer.viewmodel.ScenarioViewModel
import com.vikash.pokertrainer.viewmodel.StatsViewModel
import com.vikash.pokertrainer.viewmodel.ThemeViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scenario : Screen("scenario")
    data object GtoChart : Screen("gto_chart")
    data object PlayerType : Screen("player_type")
    data object Stats : Screen("stats")
}

@Composable
fun PokerNavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    val statsViewModel: StatsViewModel = viewModel()
    val statsState by statsViewModel.uiState.collectAsState()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                totalAttempted = statsState.totalAttempted,
                accuracy = statsState.overallAccuracy,
                isDarkMode = isDarkMode,
                onToggleTheme = { themeViewModel.toggleTheme() },
                onNavigateToScenario = { navController.navigate(Screen.Scenario.route) },
                onNavigateToGto = { navController.navigate(Screen.GtoChart.route) },
                onNavigateToPlayerType = { navController.navigate(Screen.PlayerType.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) }
            )
        }
        composable(Screen.Scenario.route) {
            val viewModel: ScenarioViewModel = viewModel()
            ScenarioScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GtoChart.route) {
            val viewModel: GtoViewModel = viewModel()
            GtoChartScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PlayerType.route) {
            val viewModel: PlayerTypeViewModel = viewModel()
            PlayerTypeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                viewModel = statsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
