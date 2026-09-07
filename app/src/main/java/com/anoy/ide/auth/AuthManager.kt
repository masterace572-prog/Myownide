package com.anoy.ide.auth

import com.anoy.ide.core.config.ForgeConfig
import com.anoy.ide.core.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.currentSessionOrNull

/**
 * Thin auth facade for Milestone 0. It wires email/password against Supabase
 * and exposes whether a session exists. Google sign-in is added in the next
 * step (requires a Google OAuth client ID configured in the Supabase project).
 */
class AuthManager(private val client: SupabaseClient = createSupabaseClientForAuth()) {

    val configured: Boolean get() = ForgeConfig.isSupabaseConfigured

    suspend fun signInWithEmail(email: String, password: String) {
        require(configured) { "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY." }
        client.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        require(configured) { "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY." }
        client.auth.signUpWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    val isSignedIn: Boolean
        get() = client.auth.currentSessionOrNull() != null

    private companion object {
        fun createSupabaseClientForAuth(): SupabaseClient {
            return if (ForgeConfig.isSupabaseConfigured) {
                createSupabaseClient()
            } else {
                // Safe no-op client with placeholder config. The UI stays
                // functional in local-only mode until real credentials arrive.
                createSupabaseClient(
                    url = "https://placeholder.supabase.co",
                    anonKey = "placeholder"
                )
            }
        }
    }
}
