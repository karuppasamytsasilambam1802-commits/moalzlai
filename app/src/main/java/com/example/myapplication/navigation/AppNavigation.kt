package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.*
import com.example.myapplication.viewmodel.VoxBridgeViewModel

object Screen {
    const val WELCOME = "welcome"
    const val LANGUAGE_SETTINGS = "language_settings"
    const val HOME = "home"
    const val CREATE_JOIN_ROOM = "create_join_room"
    const val LIVE_CONVERSATION = "live_conversation"
    const val DEMO_MODE = "demo_mode"
    const val REPORTS_ANALYTICS = "reports_analytics"
    const val ONLINE_CLASS_MEETING = "online_class_meeting"
}

@Composable
fun AppNavigation(
    viewModel: VoxBridgeViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WELCOME
    ) {
        composable(Screen.WELCOME) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.LANGUAGE_SETTINGS) },
                onDemoMode = { navController.navigate(Screen.DEMO_MODE) },
                viewModel = viewModel
            )
        }

        composable(Screen.LANGUAGE_SETTINGS) {
            LanguageSettingsScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate(Screen.HOME) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HOME) {
            HomeDashboardScreen(
                viewModel = viewModel,
                onCreateRoom = { navController.navigate(Screen.CREATE_JOIN_ROOM) },
                onJoinRoom = { navController.navigate(Screen.CREATE_JOIN_ROOM) },
                onLanguageSettings = { navController.navigate(Screen.LANGUAGE_SETTINGS) },
                onDemoMode = { navController.navigate(Screen.DEMO_MODE) },
                onReportsClick = { navController.navigate(Screen.REPORTS_ANALYTICS) },
                onOnlineClassClick = {
                    viewModel.createOnlineClass("Multilingual AI & Computer Science", "Prof. Alan Turing")
                    navController.navigate(Screen.ONLINE_CLASS_MEETING)
                }
            )
        }

        composable(Screen.CREATE_JOIN_ROOM) {
            CreateJoinRoomScreen(
                viewModel = viewModel,
                onRoomCreatedOrJoined = { navController.navigate(Screen.LIVE_CONVERSATION) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LIVE_CONVERSATION) {
            LiveConversationScreen(
                viewModel = viewModel,
                onExitConversation = {
                    viewModel.leaveRoom()
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DEMO_MODE) {
            DemoModeScreen(
                viewModel = viewModel,
                onExitDemo = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.REPORTS_ANALYTICS) {
            ReportsAnalyticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ONLINE_CLASS_MEETING) {
            OnlineClassMeetingScreen(
                viewModel = viewModel,
                onLeaveClass = {
                    viewModel.leaveRoom()
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
