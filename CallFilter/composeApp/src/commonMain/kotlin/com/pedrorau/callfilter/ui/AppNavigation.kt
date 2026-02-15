package com.pedrorau.callfilter.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pedrorau.callfilter.model.SystemState

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val RULES = "rules"
    const val PATTERN_RULE = "patternRule"
    const val BLOCKED_LIST = "blockedList"
}

@Composable
fun AppNavigation(
    systemState: SystemState = SystemState.NOT_CONFIGURED,
    isBatteryOptimized: Boolean = false,
    getNotificationPermissionResult: () -> Boolean? = { null },
    onRequestBatteryOptimization: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onNotificationPermissionConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }

    val startDestination = if (onboardingViewModel.isOnboardingCompleted()) {
        Routes.HOME
    } else {
        Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onComplete = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel { HomeViewModel() }
            homeViewModel.updateSystemState(systemState)
            homeViewModel.updateBatteryOptimization(isBatteryOptimized)
            val state by homeViewModel.state.collectAsState()
            HomeScreen(
                state = state,
                onConfigureRules = { navController.navigate(Routes.RULES) },
                onHowItWorks = { navController.navigate(Routes.ONBOARDING) },
                onRequestBatteryOptimization = onRequestBatteryOptimization
            )
        }

        composable(Routes.RULES) {
            val rulesViewModel: RulesViewModel = viewModel { RulesViewModel() }

            LaunchedEffect(Unit) {
                rulesViewModel.loadRules()
            }

            LaunchedEffect(Unit) {
                snapshotFlow { getNotificationPermissionResult() }
                    .collect { result ->
                        when (result) {
                            true -> {
                                rulesViewModel.toggleNotifications(true)
                                onNotificationPermissionConsumed()
                            }
                            false -> {
                                rulesViewModel.onNotificationPermissionDenied()
                                onNotificationPermissionConsumed()
                            }
                            null -> { /* waiting for result */ }
                        }
                    }
            }

            val state by rulesViewModel.state.collectAsState()
            RulesScreen(
                state = state,
                onToggleRule = { id, enabled -> rulesViewModel.toggleRule(id, enabled) },
                onToggleNotifications = { enabled ->
                    if (enabled) {
                        onRequestNotificationPermission()
                    } else {
                        rulesViewModel.toggleNotifications(false)
                    }
                },
                onEditPattern = { navController.navigate(Routes.PATTERN_RULE) },
                onEditBlockedList = { navController.navigate(Routes.BLOCKED_LIST) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PATTERN_RULE) {
            val patternViewModel: PatternRuleViewModel = viewModel { PatternRuleViewModel() }

            LaunchedEffect(Unit) {
                patternViewModel.loadPattern()
            }

            val state by patternViewModel.state.collectAsState()
            PatternRuleScreen(
                state = state,
                onPatternChanged = { patternViewModel.updatePattern(it) },
                onSave = { patternViewModel.savePattern() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BLOCKED_LIST) {
            val blockedListViewModel: BlockedListViewModel = viewModel { BlockedListViewModel() }

            LaunchedEffect(Unit) {
                blockedListViewModel.loadNumbers()
            }

            val state by blockedListViewModel.state.collectAsState()
            BlockedListScreen(
                state = state,
                onInputChanged = { blockedListViewModel.updateInput(it) },
                onAddNumber = { blockedListViewModel.addNumber() },
                onRemoveNumber = { blockedListViewModel.removeNumber(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
