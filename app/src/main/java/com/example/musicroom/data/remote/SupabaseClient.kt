package com.example.musicroom.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://zlfyhitymkxwldhjlzeq.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpsZnloaXR5bWt4d2xkaGpsemVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTAzNDE0NTksImV4cCI6MjA2NTkxNzQ1OX0.eeGXAZ6gzO31-Qvm2fR-JqNdivvQP3smxLKto7jyPwI"
    ) {
        install(Auth)
        install(Postgrest)
    }
}