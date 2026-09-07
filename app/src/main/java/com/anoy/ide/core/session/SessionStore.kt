package com.anoy.ide.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

/**
 * Persists the non-sensitive session state with DataStore. Access tokens are
 * kept out of here; they are handled by the Supabase client / encrypted storage
 * once the auth layer is fully wired.
 */
class SessionStore(private val context: Context) {

    val stage: Flow<String> = context.sessionDataStore.data.map { it[STAGE] ?: ONBOARDING }
    val onboardingCompleted: Flow<Boolean> = context.sessionDataStore.data.map { it[ONBOARDING_DONE] ?: false }
    val toolchainVerified: Flow<Boolean> = context.sessionDataStore.data.map { it[TOOLCHAIN_VERIFIED] ?: false }
    val theme: Flow<String> = context.sessionDataStore.data.map { it[THEME] ?: ThemePreference.SYSTEM.name }

    suspend fun setStage(value: String) {
        context.sessionDataStore.edit { it[STAGE] = value }
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.sessionDataStore.edit { it[ONBOARDING_DONE] = value }
    }

    suspend fun setToolchainVerified(value: Boolean) {
        context.sessionDataStore.edit { it[TOOLCHAIN_VERIFIED] = value }
    }

    suspend fun setTheme(value: ThemePreference) {
        context.sessionDataStore.edit { it[THEME] = value.name }
    }

    companion object {
        const val ONBOARDING = "ONBOARDING"
        const val WELCOME = "WELCOME"
        const val AUTH = "AUTH"
        const val TOOLCHAIN_SETUP = "TOOLCHAIN_SETUP"
        const val WORKSPACE = "WORKSPACE"

        private val STAGE = stringPreferencesKey("stage")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        private val TOOLCHAIN_VERIFIED = booleanPreferencesKey("toolchain_verified")
        private val THEME = stringPreferencesKey("theme")
    }
}
