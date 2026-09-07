package com.anoy.ide.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgeOutlinedButton
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeTextButton

/**
 * Welcome entry. Two stacked auth actions plus a local-only link. The Google
 * button is outlined and the email action is filled with the accent color,
 * matching the PRD hierarchy.
 */
@Composable
fun WelcomeScreen(
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onContinueWithoutAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Forge",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Build real Android apps on your phone. Set up a toolchain once, then write, compile, test and install entirely on-device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        ForgeOutlinedButton(
            text = "Continue with Google",
            onClick = onGoogle,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ForgePrimaryButton(
            text = "Continue with email",
            onClick = onEmail,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        ForgeTextButton(
            text = "Continue without an account",
            onClick = onContinueWithoutAccount,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
