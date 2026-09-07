package com.anoy.ide.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val isBusy: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val configured: Boolean = true,
    val signedIn: Boolean = false
)

class AuthViewModel(
    private val authManager: AuthManager = AuthManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(configured = authManager.configured)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, error = null, success = null) }
    }

    fun submit(email: String, password: String) {
        val mode = _uiState.value.mode
        _uiState.update { it.copy(isBusy = true, error = null, success = null) }
        viewModelScope.launch {
            runCatching {
                if (mode == AuthMode.SIGN_IN) {
                    authManager.signInWithEmail(email, password)
                } else {
                    authManager.signUpWithEmail(email, password)
                }
            }.onSuccess {
                _uiState.update {
                    if (mode == AuthMode.SIGN_IN) {
                        it.copy(
                            isBusy = false,
                            signedIn = true
                        )
                    } else {
                        it.copy(
                            isBusy = false,
                            success = "Check your email to verify your account."
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        error = error.message ?: "Something went wrong. Try again."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, success = null) }
    }
}
