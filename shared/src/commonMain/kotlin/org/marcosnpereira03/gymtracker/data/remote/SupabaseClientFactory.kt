package org.marcosnpereira03.gymtracker.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.marcosnpereira03.gymtracker.config.AppConfig

/**
 * Parámetros de configuración de conexión a Supabase cargados desde AppConfig.
 */
object SupabaseConfig {
    val DEFAULT_URL: String = AppConfig.SUPABASE_URL
    val DEFAULT_ANON_KEY: String = AppConfig.SUPABASE_ANON_KEY
}

/**
 * Fábrica para instanciar el cliente Supabase configurado con Postgrest, Auth y Storage.
 */
object SupabaseClientFactory {
    fun create(
        url: String = SupabaseConfig.DEFAULT_URL,
        anonKey: String = SupabaseConfig.DEFAULT_ANON_KEY
    ): SupabaseClient {
        val sanitizedUrl = url.trim().removeSuffix("/").removeSuffix("/rest/v1").removeSuffix("/")
        return createSupabaseClient(
            supabaseUrl = sanitizedUrl,
            supabaseKey = anonKey.trim()
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}
