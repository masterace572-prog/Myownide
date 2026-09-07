package com.anoy.ide.core.supabase

import com.anoy.ide.core.config.ForgeConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

/**
 * Factory-safe client. Uses the public URL + anon key only; the service role
 * key is never present on a device.
 */
fun createSupabaseClient(): SupabaseClient {
    return createSupabaseClient(
        url = ForgeConfig.supabaseUrl,
        anonKey = ForgeConfig.supabaseAnonKey
    )
}

fun createSupabaseClient(url: String, anonKey: String): SupabaseClient {
    return io.github.jan.supabase.createSupabaseClient(url, anonKey) {
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )

        httpEngine = OkHttp.create {}

        install(Auth) {
            flowType = FlowType.PKCE
        }
        install(Postgrest)
        install(Storage)
    }
}
