package com.gonzales.prestadmin.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://calsmefdorkbrykxjbww.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNhbHNtZWZkb3JrYnJ5a3hqYnd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2MzI2MDksImV4cCI6MjA2ODIwODYwOX0.sWhpvWKtlh6S_cXTIevicFO4yytiask2Rq-CqS3YA1c"
    ) {
        install(Postgrest) {
            defaultSchema = "prestadmin"
        }
        install(Storage)
    }
}