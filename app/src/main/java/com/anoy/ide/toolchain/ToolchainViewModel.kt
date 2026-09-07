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
    /** true = needed to compile Android projects, false = downloadable later. */
    val requiredForBuild: Boolean,
    val note: String? = null,
    val status: ComponentStatus = ComponentStatus.Pending
) {
    val isOptional: Boolean get() = !requiredForBuild
}

data class ToolchainUiState(
    val components: List<ToolchainComponent>,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val progress: Float = 0f,
    val selectedForRun: Boolean = true
)

/**
 * M0 model for toolchain setup.
 *
 * Only the components needed to compile an Android project are required:
 * a JDK, the Android SDK platform + build-tools, platform-tools, a Gradle
 * launcher, and the Kotlin compiler. Everything else (Git, NDK, extra API
 * levels, offline documentation sources) is optional and can be installed
 * later without affecting build capability.
 *
 * The download/checksum verification is simulated here; the real installer
 * (parallel downloads, HTTP Range, SHA-256, signed manifest) lands in M1.
 */
class ToolchainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ToolchainUiState(
            components = listOf(
                // Required to compile an Android project.
                ToolchainComponent("OpenJDK 17", "17.0.12", "180 MB",
                    requiredForBuild = true),
                ToolchainComponent("Android SDK platform", "API 35", "65 MB",
                    requiredForBuild = true),
                ToolchainComponent("Build-Tools", "35.0.0", "120 MB",
                    requiredForBuild = true,
                    note = "aapt2, d8, apksigner, zipalign"),
                ToolchainComponent("Platform-Tools (adb)", "35.0.2", "12 MB",
                    requiredForBuild = true),
                ToolchainComponent("Gradle launcher", "wrapper-aware", "40 MB",
                    requiredForBuild = true,
                    note = "Projects use their own Gradle wrapper version"),
                ToolchainComponent("Kotlin compiler", "2.0.0", "90 MB",
                    requiredForBuild = true),

                // Optional; not needed to compile a plain Android project.
                ToolchainComponent("Git", "2.45.0", "25 MB",
                    requiredForBuild = false,
                    note = "Version control UI"),
                ToolchainComponent("NDK (arm64)", "27.0.0", "640 MB",
                    requiredForBuild = false,
                    note = "Only for C/C++ native code"),
                ToolchainComponent("Additional API levels", "API 34", "45 MB",
                    requiredForBuild = false,
                    note = "Only if you target other SDKs"),
                ToolchainComponent("Sources for docs", "latest", "35 MB",
                    requiredForBuild = false,
                    note = "Offline documentation/completion")
            )
        )
    )

    val uiState: StateFlow<ToolchainUiState> = _uiState.asStateFlow()

    fun startSetup() {
        if (_uiState.value.isRunning) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, selectedForRun = false) }
            // Install only the required components automatically; optional ones
            // stay available in the list but are not forced during first setup.
            val required = _uiState.value.components.filter { it.requiredForBuild }
            val total = required.size
            var verified = 0
            required.forEach { target ->
                _uiState.update { state ->
                    state.copy(
                        components = state.components.map { component ->
                            if (component.name == target.name && component.requiredForBuild) {
                                component.copy(status = ComponentStatus.Installing)
                            } else {
                                component
                            }
                        }
                    )
                }
                delay(650)
                _uiState.update { state ->
                    state.copy(
                        components = state.components.map { component ->
                            if (component.name == target.name && component.requiredForBuild) {
                                component.copy(status = ComponentStatus.Verified)
                            } else {
                                component
                            }
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
