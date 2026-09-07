package com.anoy.ide.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.anoy.ide.core.ui.components.ForgeOutlinedButton
import com.anoy.ide.core.ui.components.ForgePrimaryButton
import com.anoy.ide.core.ui.components.ForgeTextButton

/**
 * Email/password authentication. Inline validation only — no modal errors, per
 * the PRD. Backend wiring (Supabase signInWithPassword / signUp) lands in M0
 * with the account layer; this scaffold validates and routes.
 */
@Composable
fun AuthEmailScreen(
    onBack: () -> Unit,
    onAuthenticated: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val emailValid = email.isNotBlank() && email.contains("@")
    val passwordValid = password.length >= 8
    val formValid = emailValid && passwordValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        ForgeTextButton(
            text = "Back",
            onClick = onBack
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sign in or create an account",
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

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
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
            onValueChange = { password = it },
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

        Spacer(modifier = Modifier.height(28.dp))

        ForgePrimaryButton(
            text = "Sign in",
            onClick = onAuthenticated,
            enabled = formValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ForgeOutlinedButton(
            text = "Create account",
            onClick = onAuthenticated,
            enabled = formValid,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        ForgeTextButton(
            text = "Forgot password?",
            onClick = { /* TODO(M0): Supabase password recovery / magic link */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
