package com.anoy.ide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anoy.ide.core.session.SessionStore
import com.anoy.ide.navigation.ForgeStage
import kotlinx.coroutines.launch

/**
 * Bridges the simple UI stage flow to the persisted DataStore state.
 */
class ForgeAppViewModel(private val sessionStore: SessionStore) : ViewModel() {

    fun saveStage(stage: ForgeStage) {
        viewModelScope.launch {
            sessionStore.setStage(stage.name)
            if (stage == ForgeStage.WELCOME || stage == ForgeStage.AUTH ||
                stage == ForgeStage.TOOLCHAIN_SETUP
            ) {
                sessionStore.setOnboardingCompleted(true)
            }
        }
    }

    fun goTo(stage: ForgeStage) {
        saveStage(stage)
    }

    fun savedToolchainComplete() {
        viewModelScope.launch {
            sessionStore.setToolchainVerified(true)
            sessionStore.setStage(ForgeStage.WORKSPACE.name)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = ForgeApplication.instance
                return ForgeAppViewModel(SessionStore(context)) as T
            }
        }
    }
}
