package com.anoy.ide.core.config

import com.anoy.ide.BuildConfig

/**
 * Runtime configuration. Everything here is safe for release builds.
 *
 * Only the public Supabase URL and anon key are embedded in the APK. The
 * service role key is never shipped to the device and is only used in edge
 * functions / server-side code.
 */
object ForgeConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY

    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
            supabaseUrl.startsWith("https://") &&
            supabaseAnonKey.isNotBlank() &&
            !supabaseAnonKey.startsWith("YOUR_")
}
