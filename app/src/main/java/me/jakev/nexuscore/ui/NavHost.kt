package me.jakev.nexuscore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import me.jakev.nexuscore.ui.assets.AssetDetailScreen
import me.jakev.nexuscore.ui.assets.AssetsScreen
import me.jakev.nexuscore.ui.dashboard.DashboardScreen
import me.jakev.nexuscore.ui.login.LoginScreen
import me.jakev.nexuscore.ui.login.LoginViewModel
import me.jakev.nexuscore.ui.login.OnboardingScreen
import me.jakev.nexuscore.ui.login.PendingApprovalScreen
import me.jakev.nexuscore.ui.events.EventsScreen
import me.jakev.nexuscore.ui.reports.ReportsScreen
import me.jakev.nexuscore.ui.settings.SettingsScreen
import me.jakev.nexuscore.ui.team.TeamScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object PendingApproval : Screen("pending_approval")
    object Dashboard : Screen("dashboard")
    object Assets : Screen("assets")
    object AssetDetail : Screen("assets/{assetId}") {
        fun route(id: String) = "assets/$id"
    }
    object Events : Screen("events")
    object Team : Screen("team")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun NexusCoreNavHost() {
    val navController = rememberNavController()
    val loginVm: LoginViewModel = hiltViewModel()
    val authState by loginVm.authState.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginVm,
                onAuthenticated = { orgStatus ->
                    when (orgStatus) {
                        "PENDING" -> navController.navigate(Screen.PendingApproval.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        else -> navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNeedsOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onDone = {
                navController.navigate(Screen.PendingApproval.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.PendingApproval.route) {
            PendingApprovalScreen(onSignOut = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Assets.route) {
            AssetsScreen(
                navController = navController,
                onSignOut = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable(
            route = Screen.AssetDetail.route,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStack ->
            val assetId = backStack.arguments?.getString("assetId") ?: return@composable
            AssetDetailScreen(assetId = assetId, onBack = { navController.popBackStack() })
        }
        composable(Screen.Events.route) {
            EventsScreen(navController = navController)
        }
        composable(Screen.Team.route) {
            TeamScreen(navController = navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onSignOut = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
