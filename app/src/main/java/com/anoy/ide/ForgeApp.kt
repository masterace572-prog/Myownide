package com.anoy.ide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anoy.ide.auth.AuthEmailScreen
import com.anoy.ide.auth.WelcomeScreen
import com.anoy.ide.core.session.SessionStore
import com.anoy.ide.core.session.ThemePreference
import com.anoy.ide.navigation.ForgeStage
import kotlinx.coroutines.launch
import com.anoy.ide.onboarding.OnboardingScreen
import com.anoy.ide.toolchain.ToolchainSetupScreen
import com.anoy.ide.workspace.WorkspaceScreen

/**
 * Top-level flow. The stage is persisted in DataStore so the user returns to
 * where they left off after a restart or process death.
 */
@Composable
fun ForgeApp(
    sessionStore: SessionStore,
    viewModel: ForgeAppViewModel = viewModel<ForgeAppViewModel>(factory = ForgeAppViewModel.Factory)
) {
    val stage by sessionStore.stage.collectAsState(initial = ForgeStage.ONBOARDING.name)
    val themeName by sessionStore.theme.collectAsState(
        initial = ThemePreference.SYSTEM.name
    )
    val scope = rememberCoroutineScope()

    when (stage) {
        ForgeStage.ONBOARDING.name -> OnboardingScreen(
            onFinished = { viewModel.goTo(ForgeStage.WELCOME) }
        )

        ForgeStage.WELCOME.name -> WelcomeScreen(
            onGoogle = { viewModel.saveStage(ForgeStage.WELCOME) /* TODO(M0): Google sign-in */ },
            onEmail = { viewModel.saveStage(ForgeStage.AUTH) },
            onContinueWithoutAccount = { viewModel.saveStage(ForgeStage.TOOLCHAIN_SETUP) }
        )

        ForgeStage.AUTH.name -> AuthEmailScreen(
            onBack = { viewModel.saveStage(ForgeStage.WELCOME) },
            onAuthenticated = { viewModel.saveStage(ForgeStage.TOOLCHAIN_SETUP) }
        )

        ForgeStage.TOOLCHAIN_SETUP.name -> ToolchainSetupScreen(
            onComplete = { viewModel.savedToolchainComplete() }
        )

        ForgeStage.WORKSPACE.name -> WorkspaceScreen(
            theme = ThemePreference.from(themeName),
            onThemeChange = { scope.launch { sessionStore.setTheme(it) } },
            onSignOut = { viewModel.saveStage(ForgeStage.WELCOME) }
        )
    }
}
