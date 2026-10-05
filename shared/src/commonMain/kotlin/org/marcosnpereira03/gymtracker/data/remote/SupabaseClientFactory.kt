package org.marcosnpereira03.gymtracker.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Parámetros de configuración de conexión a Supabase.
 */
object SupabaseConfig {
    const val DEFAULT_URL = "https://osuvwawiukckemqxbqsc.supabase.co"
    const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zdXZ3YXdpdWtja2VtcXhicXNjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTEyMTg4OTgsImV4cCI6MjEwNjc5NDg5OH0.TTDJpMBDnXfmOwzlCgY1tp63bNQK44l9JwSTihFv5O4"
}

/**
 * Fábrica para instanciar el cliente Supabase configurado con Postgrest y Auth.
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
        }
    }
}

