package com.anoy.ide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.anoy.ide.auth.AuthEmailScreen
import com.anoy.ide.auth.WelcomeScreen
import com.anoy.ide.navigation.ForgeStage
import com.anoy.ide.onboarding.OnboardingScreen
import com.anoy.ide.toolchain.ToolchainSetupScreen
import com.anoy.ide.workspace.WorkspaceScreen

/**
 * Top-level flow for Milestone 0.
 *
 * The router is intentionally a simple state machine for now so the product
 * skeleton can be evaluated before Supabase auth and persisted session state are
 * introduced.
 */
@Composable
fun ForgeApp() {
    var stageName by rememberSaveable {
        mutableStateOf(ForgeStage.ONBOARDING.name)
    }

    val stage = ForgeStage.valueOf(stageName)
    when (stage) {
        ForgeStage.ONBOARDING -> OnboardingScreen(
            onFinished = { stageName = ForgeStage.WELCOME.name }
        )

        ForgeStage.WELCOME -> WelcomeScreen(
            onGoogle = { /* TODO(M0): Supabase Google sign-in via Credential Manager */ },
            onEmail = { stageName = ForgeStage.AUTH.name },
            onContinueWithoutAccount = { stageName = ForgeStage.TOOLCHAIN_SETUP.name }
        )

        ForgeStage.AUTH -> AuthEmailScreen(
            onBack = { stageName = ForgeStage.WELCOME.name },
            onAuthenticated = { stageName = ForgeStage.TOOLCHAIN_SETUP.name }
        )

        ForgeStage.TOOLCHAIN_SETUP -> ToolchainSetupScreen(
            onComplete = { stageName = ForgeStage.WORKSPACE.name }
        )

        ForgeStage.WORKSPACE -> WorkspaceScreen(
            onSignOut = { stageName = ForgeStage.WELCOME.name }
        )
    }
}
