package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.util.BiometricAuthManager
import com.example.util.BiometricAuthResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CardVaultApp(
    viewModel: CardVaultViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Hoisted scroll states preserve scroll position across Home <-> Settings tab switches
    val homeScrollState = rememberScrollState()
    val settingsScrollState = rememberScrollState()

    // Auto-lock expanded sensitive card details whenever the app goes to background
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.collapseExpandedCard()
    }

    // System back on Settings returns cleanly to Home tab
    BackHandler(enabled = uiState.activeTab == VaultTab.SETTINGS) {
        viewModel.selectTab(VaultTab.HOME)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CardVaultEvent.CardAdded -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.settings_snackbar_added, event.cardName),
                        actionLabel = context.getString(R.string.settings_snackbar_view),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.selectTab(VaultTab.HOME)
                    }
                }
            }
        }
    }

    val handleCardToggle: (Long) -> Unit = { cardId ->
        if (uiState.expandedCardId == cardId) {
            // Hiding sensitive card details is immediate and does not require re-authentication
            viewModel.collapseExpandedCard()
        } else {
            val targetCard = uiState.allCards.firstOrNull { it.id == cardId }
            val cardLabel = targetCard?.cardName ?: context.getString(R.string.app_name)
            BiometricAuthManager.authenticateForCard(
                context = context,
                cardName = cardLabel
            ) { result ->
                when (result) {
                    BiometricAuthResult.Success -> {
                        viewModel.expandCard(cardId)
                    }

                    BiometricAuthResult.Cancelled -> {
                        // Keep card collapsed silently when user cancels prompt
                    }

                    BiometricAuthResult.NotEnrolled -> {
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val action = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.biometric_error_not_enrolled),
                                actionLabel = context.getString(R.string.biometric_action_enroll),
                                duration = SnackbarDuration.Long
                            )
                            if (action == SnackbarResult.ActionPerformed) {
                                BiometricAuthManager.openBiometricEnrollSettings(context)
                            }
                        }
                    }

                    BiometricAuthResult.Unavailable -> {
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.biometric_error_unavailable),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }

                    is BiometricAuthResult.Error -> {
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = context.getString(
                                    R.string.biometric_error_generic,
                                    result.message
                                ),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 840.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                if (!useNavigationRail) {
                    CardVaultBottomBar(
                        activeTab = uiState.activeTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    CardVaultNavigationRail(
                        activeTab = uiState.activeTab,
                        onSelectTab = viewModel::selectTab
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Crossfade(
                        targetState = uiState.activeTab,
                        animationSpec = tween(durationMillis = 200),
                        label = "vault_tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            VaultTab.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    scrollState = homeScrollState,
                                    onToggleNetwork = viewModel::toggleNetworkFilter,
                                    onToggleVariant = viewModel::toggleVariantFilter,
                                    onClearFilters = viewModel::clearAllFilters,
                                    onToggleCardExpand = handleCardToggle,
                                    onDeleteCard = viewModel::deleteCard,
                                    onNavigateToAddCard = { viewModel.selectTab(VaultTab.SETTINGS) }
                                )
                            }

                            VaultTab.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    scrollState = settingsScrollState,
                                    onCardNameChanged = viewModel::onCardNameChanged,
                                    onCardNumberChanged = viewModel::onCardNumberChanged,
                                    onExpiryChanged = viewModel::onExpiryChanged,
                                    onCvvChanged = viewModel::onCvvChanged,
                                    onCardTypeSelected = viewModel::onCardTypeSelected,
                                    onNetworkSelected = viewModel::onNetworkSelected,
                                    onVariantInputChanged = viewModel::onVariantInputChanged,
                                    onVariantSuggestionPicked = viewModel::onVariantSuggestionPicked,
                                    onSubmitCard = viewModel::submitCard,
                                    onResetForm = viewModel::resetForm
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardVaultBottomBar(
    activeTab: VaultTab,
    onSelectTab: (VaultTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 4.dp
    ) {
        val homeSelected = activeTab == VaultTab.HOME
        NavigationBarItem(
            selected = homeSelected,
            onClick = { onSelectTab(VaultTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (homeSelected) {
                        Icons.Filled.AccountBalanceWallet
                    } else {
                        Icons.Outlined.AccountBalanceWallet
                    },
                    contentDescription = stringResource(R.string.nav_home)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_home),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        val settingsSelected = activeTab == VaultTab.SETTINGS
        NavigationBarItem(
            selected = settingsSelected,
            onClick = { onSelectTab(VaultTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (settingsSelected) {
                        Icons.Filled.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}

@Composable
private fun CardVaultNavigationRail(
    activeTab: VaultTab,
    onSelectTab: (VaultTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        val homeSelected = activeTab == VaultTab.HOME
        NavigationRailItem(
            selected = homeSelected,
            onClick = { onSelectTab(VaultTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (homeSelected) {
                        Icons.Filled.AccountBalanceWallet
                    } else {
                        Icons.Outlined.AccountBalanceWallet
                    },
                    contentDescription = stringResource(R.string.nav_home)
                )
            },
            label = { Text(stringResource(R.string.nav_home)) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_rail_home")
        )

        val settingsSelected = activeTab == VaultTab.SETTINGS
        NavigationRailItem(
            selected = settingsSelected,
            onClick = { onSelectTab(VaultTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (settingsSelected) {
                        Icons.Filled.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = stringResource(R.string.nav_settings)
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_rail_settings")
        )
    }
}
