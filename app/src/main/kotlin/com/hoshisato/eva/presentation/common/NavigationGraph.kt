package com.hoshisato.eva.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.hoshisato.eva.data.model.ApiType
import com.hoshisato.eva.presentation.ui.chat.ChatScreen
import com.hoshisato.eva.presentation.ui.home.HomeScreen
import com.hoshisato.eva.presentation.ui.setting.AboutScreen
import com.hoshisato.eva.presentation.ui.setting.LicenseScreen
import com.hoshisato.eva.presentation.ui.setting.PlatformSettingScreen
import com.hoshisato.eva.presentation.ui.setting.SettingScreen
import com.hoshisato.eva.presentation.ui.setting.SettingViewModel
import com.hoshisato.eva.presentation.ui.setup.SelectModelScreen
import com.hoshisato.eva.presentation.ui.setup.SelectPlatformScreen
import com.hoshisato.eva.presentation.ui.setup.SetupAPIUrlScreen
import com.hoshisato.eva.presentation.ui.setup.SetupCompleteScreen
import com.hoshisato.eva.presentation.ui.setup.SetupViewModel
import com.hoshisato.eva.presentation.ui.setup.TokenInputScreen
import com.hoshisato.eva.presentation.ui.startscreen.StartScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Route.CHAT_LIST
    ) {
        homeScreenNavigation(navController)
        startScreenNavigation(navController)
        setupNavigation(navController)
        settingNavigation(navController)
        chatScreenNavigation(navController)
    }
}

fun NavGraphBuilder.startScreenNavigation(navController: NavHostController) {
    composable(Route.GET_STARTED) {
        StartScreen { navController.navigate(Route.SETUP_ROUTE) }
    }
}

fun NavGraphBuilder.setupNavigation(
    navController: NavHostController
) {
    navigation(startDestination = Route.SELECT_PLATFORM, route = Route.SETUP_ROUTE) {
        composable(route = Route.SELECT_PLATFORM) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectPlatformScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.TOKEN_INPUT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            TokenInputScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OPENAI_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OPENAI_MODEL_SELECT,
                platformType = ApiType.OPENAI,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OLLAMA_MODEL_SELECT) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SelectModelScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OLLAMA_MODEL_SELECT,
                platformType = ApiType.OLLAMA,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.OLLAMA_API_ADDRESS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SetupAPIUrlScreen(
                setupViewModel = setupViewModel,
                currentRoute = Route.OLLAMA_API_ADDRESS,
                onNavigate = { route -> navController.navigate(route) },
                onBackAction = { navController.navigateUp() }
            )
        }
        composable(route = Route.SETUP_COMPLETE) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETUP_ROUTE)
            }
            val setupViewModel: SetupViewModel = hiltViewModel(parentEntry)
            SetupCompleteScreen(
                setupViewModel = setupViewModel,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Route.GET_STARTED) { inclusive = true }
                    }
                },
                onBackAction = { navController.navigateUp() }
            )
        }
    }
}

fun NavGraphBuilder.homeScreenNavigation(navController: NavHostController) {
    composable(Route.CHAT_LIST) {
        HomeScreen(
            settingOnClick = { navController.navigate(Route.SETTING_ROUTE) { launchSingleTop = true } },
            onExistingChatClick = { chatRoom ->
                val enabledPlatformString = chatRoom.enabledPlatform.joinToString(",") { v -> v.name }
                navController.navigate(
                    Route.CHAT_ROOM
                        .replace(oldValue = "{chatRoomId}", newValue = "${chatRoom.id}")
                        .replace(oldValue = "{enabledPlatforms}", newValue = enabledPlatformString)
                )
            },
            navigateToNewChat = {
                val enabledPlatformString = it.joinToString(",") { v -> v.name }
                navController.navigate(
                    Route.CHAT_ROOM
                        .replace(oldValue = "{chatRoomId}", newValue = "0")
                        .replace(oldValue = "{enabledPlatforms}", newValue = enabledPlatformString)
                )
            }
        )
    }
}

fun NavGraphBuilder.chatScreenNavigation(navController: NavHostController) {
    composable(
        Route.CHAT_ROOM,
        arguments = listOf(
            navArgument("chatRoomId") { type = NavType.IntType },
            navArgument("enabledPlatforms") { defaultValue = "" }
        )
    ) {
        ChatScreen(
            onBackAction = { navController.navigateUp() }
        )
    }
}

fun NavGraphBuilder.settingNavigation(navController: NavHostController) {
    navigation(startDestination = Route.SETTINGS, route = Route.SETTING_ROUTE) {
        composable(Route.SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            SettingScreen(
                settingViewModel = settingViewModel,
                onNavigationClick = { navController.navigateUp() },
                onNavigateToPlatformSetting = { apiType -> navController.navigate(Route.OLLAMA_SETTINGS) },
                onNavigateToAboutPage = { navController.navigate(Route.ABOUT_PAGE) }
            )
        }
        composable(Route.OPENAI_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.OPENAI
            ) { navController.navigateUp() }
        }
        composable(Route.OLLAMA_SETTINGS) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Route.SETTING_ROUTE)
            }
            val settingViewModel: SettingViewModel = hiltViewModel(parentEntry)
            PlatformSettingScreen(
                settingViewModel = settingViewModel,
                apiType = ApiType.OLLAMA
            ) { navController.navigateUp() }
        }
        composable(Route.ABOUT_PAGE) {
            AboutScreen(
                onNavigationClick = { navController.navigateUp() },
                onNavigationToLicense = { navController.navigate(Route.LICENSE) }
            )
        }
        composable(Route.LICENSE) {
            LicenseScreen(onNavigationClick = { navController.navigateUp() })
        }
    }
}
