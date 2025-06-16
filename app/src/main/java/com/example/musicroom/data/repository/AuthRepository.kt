package com.example.musicroom.data.repository

import android.util.Log
import com.example.musicroom.data.models.User
import com.example.musicroom.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {
      suspend fun signUp(email: String, password: String, fullName: String): Result<User> {
        Log.d("AuthRepository", "Starting signUp for email: $email")
        return try {
            Log.d("AuthRepository", "Calling Supabase signUpWith")
            
            // Call signUp - this creates the user in Supabase
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
            Log.d("AuthRepository", "Supabase signUp completed successfully")
            
            // After signup, try to get the current user session
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            Log.d("AuthRepository", "Current user after signup: $currentUser")
            
            if (currentUser != null) {
                val user = User(
                    id = currentUser.id,
                    name = fullName,
                    username = email.substringBefore("@"),
                    photoUrl = "",
                    email = email
                )
                Log.d("AuthRepository", "User created successfully: $user")
                Result.success(user)
            } else {
                // This is common with Supabase - user might need email verification
                Log.w("AuthRepository", "User created but not immediately available - might need email verification")
                val user = User(
                    id = "pending_${System.currentTimeMillis()}", // Temporary ID
                    name = fullName,
                    username = email.substringBefore("@"),
                    photoUrl = "",
                    email = email
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during signUp: ${e.message}", e)
            Result.failure(e)
        }
    }
      suspend fun signIn(email: String, password: String): Result<User> {
        Log.d("AuthRepository", "Starting signIn for email: $email")
        return try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            if (currentUser != null) {
                val fullName = currentUser.userMetadata?.get("full_name")?.toString() ?: ""
                val user = User(
                    id = currentUser.id,
                    name = fullName,
                    username = email.substringBefore("@"),
                    photoUrl = "",
                    email = email
                )
                Log.d("AuthRepository", "User signed in successfully: $user")
                Result.success(user)
            } else {
                Log.e("AuthRepository", "Sign in failed - currentUser is null")
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during signIn: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            SupabaseClient.client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): User? {
        val supabaseUser = SupabaseClient.client.auth.currentUserOrNull()
        return if (supabaseUser != null) {
            val fullName = supabaseUser.userMetadata?.get("full_name")?.toString() ?: ""
            User(
                id = supabaseUser.id,
                name = fullName,
                username = supabaseUser.email?.substringBefore("@") ?: "",
                photoUrl = "",
                email = supabaseUser.email ?: ""
            )
        } else {
            null
        }
    }
}
