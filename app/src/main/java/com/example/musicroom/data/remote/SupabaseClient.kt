package com.example.musicroom.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://kentgeohfakhpulwpjqk.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtlbnRnZW9oZmFraHB1bHdwanFrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAwODIzOTMsImV4cCI6MjA2NTY1ODM5M30.FTC2MlRxXm3TuEYbgXiQbJ7qfrA_PNe5W980jlGYO5A"
    ) {
        install(Auth)
        install(Postgrest)
    }
}