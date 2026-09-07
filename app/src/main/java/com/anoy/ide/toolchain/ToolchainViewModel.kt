package com.anoy.ide.toolchain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ComponentStatus {
    Pending,
    Installing,
    Verified,
    Error
}

data class ToolchainComponent(
    val name: String,
    val version: String,
    val size: String,
    val optional: Boolean,
    val status: ComponentStatus = ComponentStatus.Pending
)

data class ToolchainUiState(
    val components: List<ToolchainComponent>,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val selectedForRun: Boolean = true
)

/**
 * M0 model for toolchain setup. The download, checksum verification and post
 * install verification are simulated here; the real installer (parallel
 * downloads, HTTP Range, SHA-256, signed manifest) lands in M1.
 */
class ToolchainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ToolchainUiState(
            components = listOf(
                ToolchainComponent("OpenJDK 17", "17.0.12", "180 MB", false),
                ToolchainComponent("Android SDK platform", "API 35", "65 MB", false),
                ToolchainComponent("Build-Tools", "35.0.0", "120 MB", false),
                ToolchainComponent("Platform-Tools (adb)", "35.0.2", "12 MB", false),
                ToolchainComponent("Gradle", "8.9", "130 MB", false),
                ToolchainComponent("Kotlin compiler", "2.0.0", "90 MB", false),
                ToolchainComponent("Git", "2.45.0", "25 MB", false),
                ToolchainComponent("NDK (arm64)", "27.0.0", "640 MB", true),
                ToolchainComponent("Additional API levels", "API 34", "45 MB", true)
            )
        )
    )

    val uiState: StateFlow<ToolchainUiState> = _uiState.asStateFlow()

    fun startSetup() {
        if (_uiState.value.isRunning) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, selectedForRun = false) }
            val total = _uiState.value.components.size
            var verified = 0
            _uiState.value.components.indices.forEach { index ->
                _uiState.update { state ->
                    state.copy(
                        components = state.components.mapIndexed { i, component ->
                            if (i == index) component.copy(status = ComponentStatus.Installing) else component
                        }
                    )
                }
                delay(650)
                _uiState.update { state ->
                    state.copy(
                        components = state.components.mapIndexed { i, component ->
                            if (i == index) component.copy(status = ComponentStatus.Verified) else component
                        },
                        progress = verified.toFloat() / total
                    )
                }
                verified += 1
            }
            _uiState.update {
                it.copy(
                    isRunning = false,
                    selectedForRun = true,
                    progress = 1f,
                    isComplete = true
                )
            }
        }
    }
}
