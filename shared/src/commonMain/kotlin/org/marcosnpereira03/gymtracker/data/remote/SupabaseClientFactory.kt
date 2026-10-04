package org.marcosnpereira03.gymtracker.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Parámetros de configuración de conexión a Supabase.
 */
object SupabaseConfig {
    const val DEFAULT_URL = "https://your-project-id.supabase.co"
    const val DEFAULT_ANON_KEY = "your-anon-key"
}

/**
 * Fábrica para instanciar el cliente Supabase configurado con el módulo Postgrest.
 */
object SupabaseClientFactory {
    fun create(
        url: String = SupabaseConfig.DEFAULT_URL,
        anonKey: String = SupabaseConfig.DEFAULT_ANON_KEY
    ): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Postgrest)
        }
    }
}
