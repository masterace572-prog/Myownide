package com.anoy.ide.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anoy.ide.core.ui.components.ForgeOutlinedButton
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeTextButton

/**
 * Email/password authentication. Inline validation only — no modal errors.
 */
@Composable
fun AuthEmailScreen(
    onBack: () -> Unit,
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    val emailValid = email.isNotBlank() && email.contains("@")
    val passwordValid = password.length >= 8
    val formValid = emailValid && passwordValid && uiState.configured

    LaunchedEffect(uiState.signedIn) {
        if (uiState.signedIn) {
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        ForgeTextButton(text = "Back", onClick = onBack)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (uiState.mode == AuthMode.SIGN_IN) "Sign in to Forge" else "Create your account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your account syncs settings and project metadata. Source code stays on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        RowOfModeButtons(viewModel = viewModel)

        if (!uiState.configured) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cloud sign-in isn't configured on this build. You can continue in local-only mode.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = email.isNotBlank() && !emailValid,
            supportingText = {
                if (email.isNotBlank() && !emailValid) {
                    Text("Enter a valid email address.")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotBlank() && !passwordValid,
            supportingText = {
                if (password.isNotBlank() && !passwordValid) {
                    Text("Use at least 8 characters.")
                }
            }
        )

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.success != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.success!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        ForgePrimaryButton(
            text = if (uiState.mode == AuthMode.SIGN_IN) "Sign in" else "Create account",
            onClick = { viewModel.submit(email, password) },
            enabled = formValid && !uiState.isBusy,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ForgeTextButton(
            text = "Forgot password?",
            onClick = { /* TODO(M0): Supabase password recovery / magic link */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RowOfModeButtons(viewModel: AuthViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        ForgeOutlinedButton(
            text = "Sign in",
            onClick = { viewModel.setMode(AuthMode.SIGN_IN) },
            enabled = viewModel.uiState.value.mode != AuthMode.SIGN_IN
        )
        ForgeOutlinedButton(
            text = "Create account",
            onClick = { viewModel.setMode(AuthMode.SIGN_UP) },
            enabled = viewModel.uiState.value.mode != AuthMode.SIGN_UP,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
