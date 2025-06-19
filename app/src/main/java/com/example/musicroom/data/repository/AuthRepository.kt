package com.example.musicroom.data.repository

import android.content.Intent
import android.util.Log
import com.example.musicroom.data.models.User
import com.example.musicroom.data.remote.SupabaseClient
import com.example.musicroom.data.auth.GoogleAuthUiClient
import com.example.musicroom.data.auth.GoogleSignInResult
import com.example.musicroom.data.auth.DeepLinkManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val googleAuthUiClient: GoogleAuthUiClient
) {
      suspend fun signUp(email: String, password: String, fullName: String): Result<User> {
        Log.d("AuthRepository", "=== STARTING SIGNUP ===")
        Log.d("AuthRepository", "Email: $email")
        Log.d("AuthRepository", "Full Name: $fullName")
        Log.d("AuthRepository", "Password length: ${password.length}")
        
        return try {
            Log.d("AuthRepository", "Calling Supabase signUpWith...")
            
            // Call signUp - this creates the user in Supabase
            val signUpResult = SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("full_name", fullName)
                    put("username", email.substringBefore("@"))
                }
            }
            
            Log.d("AuthRepository", "Supabase signUp completed successfully")
            Log.d("AuthRepository", "SignUp result: $signUpResult")
            
            // Give the database trigger time to create profile data
            delay(1000)
            
            // After signup, try to get the current user session
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            Log.d("AuthRepository", "Current user after signup: ${currentUser?.id}")
            Log.d("AuthRepository", "User email confirmed: ${currentUser?.emailConfirmedAt}")
            Log.d("AuthRepository", "User metadata: ${currentUser?.userMetadata}")
            
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
                Log.w("AuthRepository", "User created but not immediately available - likely needs email verification")
                val user = User(
                    id = "pending_${System.currentTimeMillis()}", // Temporary ID
                    name = fullName,
                    username = email.substringBefore("@"),
                    photoUrl = "",
                    email = email
                )
                Log.d("AuthRepository", "Returning pending user: $user")
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== SIGNUP FAILED ===")
            Log.e("AuthRepository", "Exception type: ${e::class.simpleName}")
            Log.e("AuthRepository", "Exception message: ${e.message}")
            Log.e("AuthRepository", "Stack trace: ${e.stackTraceToString()}")
            
            // Parse the error message to provide better user feedback
            val userFriendlyMessage = when {
                e.message?.contains("already registered", true) == true -> 
                    "An account with this email already exists. Please try signing in instead."
                e.message?.contains("invalid", true) == true -> 
                    "Invalid email or password format. Please check your input."
                e.message?.contains("network", true) == true || e.message?.contains("connection", true) == true -> 
                    "Network error. Please check your internet connection and try again."
                e.message?.contains("database", true) == true -> 
                    "Database error occurred while creating your account. Please try again."
                else -> "Sign up failed: ${e.message ?: "Unknown error"}"
            }
            
            Log.e("AuthRepository", "User-friendly error: $userFriendlyMessage")
            Result.failure(Exception(userFriendlyMessage))
        }
    }
      suspend fun signIn(email: String, password: String): Result<User> {
        Log.d("AuthRepository", "=== STARTING SIGNIN ===")
        Log.d("AuthRepository", "Email: $email")
        Log.d("AuthRepository", "Password length: ${password.length}")
        
        return try {
            Log.d("AuthRepository", "Calling Supabase signInWith...")
            
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            Log.d("AuthRepository", "Supabase signIn completed successfully")
            
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            Log.d("AuthRepository", "Current user after signin: ${currentUser?.id}")
            Log.d("AuthRepository", "User email confirmed: ${currentUser?.emailConfirmedAt}")
            Log.d("AuthRepository", "User metadata: ${currentUser?.userMetadata}")
            
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
                Result.failure(Exception("Sign in failed - unable to retrieve user session"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "=== SIGNIN FAILED ===")
            Log.e("AuthRepository", "Exception type: ${e::class.simpleName}")
            Log.e("AuthRepository", "Exception message: ${e.message}")
            Log.e("AuthRepository", "Stack trace: ${e.stackTraceToString()}")
            
            // Parse the error message to provide better user feedback
            val userFriendlyMessage = when {
                e.message?.contains("invalid login credentials", true) == true -> 
                    "Invalid email or password. Please check your credentials and try again."
                e.message?.contains("email not confirmed", true) == true -> 
                    "Please check your email and confirm your account before signing in."
                e.message?.contains("network", true) == true || e.message?.contains("connection", true) == true -> 
                    "Network error. Please check your internet connection and try again."
                e.message?.contains("too many requests", true) == true -> 
                    "Too many login attempts. Please wait a moment and try again."
                else -> "Sign in failed: ${e.message ?: "Unknown error"}"
            }
            
            Log.e("AuthRepository", "User-friendly error: $userFriendlyMessage")
            Result.failure(Exception(userFriendlyMessage))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            SupabaseClient.client.auth.signOut()
            googleAuthUiClient.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoogleSignInIntent() = googleAuthUiClient.getSignInIntent()

    suspend fun handleGoogleSignInResult(data: Intent?): Result<User> {
        Log.d("AuthRepository", "Handling Google Sign-In result")
        return try {
            val googleSignInResult = googleAuthUiClient.signInWithIntent(data)
            
            if (googleSignInResult.data != null) {
                val googleUser = googleSignInResult.data
                Log.d("AuthRepository", "Google user data: ${googleUser.username}, ${googleUser.email}")
                
                // Try to authenticate with Supabase using Google ID token
                if (googleUser.idToken != null) {
                    Log.d("AuthRepository", "Attempting Supabase authentication with Google ID token")
                    try {
                        // Authenticate with Supabase using Google ID token
                        SupabaseClient.client.auth.signInWith(IDToken) {
                            this.idToken = googleUser.idToken!!
                        }
                        
                        // Get the authenticated user from Supabase
                        val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                        if (currentUser != null) {
                            val user = User(
                                id = currentUser.id,
                                name = googleUser.username ?: currentUser.userMetadata?.get("full_name")?.toString() ?: "",
                                username = googleUser.email?.substringBefore("@") ?: "",
                                photoUrl = googleUser.profilePictureUrl ?: "",
                                email = googleUser.email ?: currentUser.email ?: ""
                            )
                            Log.d("AuthRepository", "Google Sign-In with Supabase successful: $user")
                            return Result.success(user)
                        } else {
                            Log.e("AuthRepository", "Failed to get user from Supabase after Google authentication")
                        }
                    } catch (supabaseError: Exception) {
                        Log.e("AuthRepository", "Supabase authentication with ID token failed: ${supabaseError.message}", supabaseError)
                    }
                }
                  // Fallback: Create user in Supabase using email/password approach
                Log.d("AuthRepository", "Fallback: Creating user in Supabase with email/password")
                try {
                    // Generate a unique password for Google users
                    val tempPassword = "google_${googleUser.userId}_${System.currentTimeMillis()}"
                    
                    // Try to sign up first (in case user doesn't exist)
                    try {
                        SupabaseClient.client.auth.signUpWith(Email) {
                            this.email = googleUser.email ?: ""
                            this.password = tempPassword
                            this.data = buildJsonObject {
                                put("full_name", googleUser.username ?: "")
                                put("google_id", googleUser.userId)
                                put("profile_picture", googleUser.profilePictureUrl ?: "")
                                put("auth_provider", "google")
                            }
                        }
                        Log.d("AuthRepository", "Created new user in Supabase via Google Sign-In")
                    } catch (signUpError: Exception) {
                        Log.d("AuthRepository", "User might already exist, trying sign in: ${signUpError.message}")
                        // If signup fails, try to sign in (user might already exist)
                        SupabaseClient.client.auth.signInWith(Email) {
                            this.email = googleUser.email ?: ""
                            this.password = tempPassword
                        }
                    }
                    
                    // Get the authenticated user from Supabase
                    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                    if (currentUser != null) {
                        val user = User(
                            id = currentUser.id,
                            name = googleUser.username ?: currentUser.userMetadata?.get("full_name")?.toString() ?: "",
                            username = googleUser.email?.substringBefore("@") ?: "",
                            photoUrl = googleUser.profilePictureUrl ?: "",
                            email = googleUser.email ?: currentUser.email ?: ""
                        )
                        Log.d("AuthRepository", "Google user saved to Supabase successfully: $user")
                        return Result.success(user)
                    } else {
                        Log.e("AuthRepository", "Failed to authenticate user with Supabase after creation")
                    }
                } catch (fallbackError: Exception) {
                    Log.e("AuthRepository", "Fallback Supabase authentication failed: ${fallbackError.message}", fallbackError)
                }
                
                // Last resort: return local user (but this won't be saved to database)
                val user = User(
                    id = googleUser.userId,
                    name = googleUser.username ?: "",
                    username = googleUser.email?.substringBefore("@") ?: "",
                    photoUrl = googleUser.profilePictureUrl ?: "",
                    email = googleUser.email ?: ""
                )
                Log.w("AuthRepository", "Returning local user (not saved to database): $user")
                Result.success(user)
            } else {
                val errorMessage = googleSignInResult.errorMessage ?: "Google Sign-In failed"
                Log.e("AuthRepository", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during Google Sign-In result handling: ${e.message}", e)
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
    }    suspend fun resetPassword(email: String): Result<String> {
        Log.d("AuthRepository", "Starting password reset for email: $email")
        return try {
            // Note: The redirect URL should be configured in Supabase dashboard
            // under Authentication -> URL Configuration -> Redirect URLs
            SupabaseClient.client.auth.resetPasswordForEmail(email)
            Log.d("AuthRepository", "Password reset email sent successfully")
            Result.success("Password reset instructions sent to your email")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during password reset: ${e.message}", e)
            Result.failure(e)
        }
    }suspend fun updatePassword(newPassword: String, accessToken: String): Result<User> {
        Log.d("AuthRepository", "Starting password update with access token")
        return try {
            Log.d("AuthRepository", "Access token: ${accessToken.take(20)}...")
            Log.d("AuthRepository", "Full access token length: ${accessToken.length}")
            
            // Validate the access token format
            if (!accessToken.startsWith("eyJ")) {
                Log.e("AuthRepository", "Invalid access token format - should be JWT starting with 'eyJ'")
                return Result.failure(Exception("Invalid access token format"))
            }
            
            // For password reset, we try different approaches to establish session context
            try {
                // Method 1: Try to use retrieveUser to set context
                Log.d("AuthRepository", "Attempting to set user context with access token")
                SupabaseClient.client.auth.retrieveUser(accessToken)
                
                // Now update the password
                Log.d("AuthRepository", "Updating password...")
                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                
                // Get the current user after password update
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val fullName = currentUser.userMetadata?.get("full_name")?.toString() ?: ""
                    val user = User(
                        id = currentUser.id,
                        name = fullName,
                        username = currentUser.email?.substringBefore("@") ?: "",
                        photoUrl = "",
                        email = currentUser.email ?: ""
                    )
                    Log.d("AuthRepository", "Password updated successfully for user: $user")
                    Result.success(user)
                } else {
                    Log.d("AuthRepository", "Password update completed but currentUser is null")
                    // Password might have been updated successfully but session not established
                    // This is actually common with password reset flows
                    Result.success(User(
                        id = "password_reset_success",
                        name = "Password Reset",
                        username = "success",
                        photoUrl = "",
                        email = "reset@success.com"
                    ))
                }
                
            } catch (e: Exception) {
                Log.e("AuthRepository", "Password update failed: ${e.message}")
                Result.failure(e)
            }
            
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during password update: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun confirmEmail(accessToken: String): Result<User> {
        Log.d("AuthRepository", "Starting email confirmation with access token")
        return try {
            Log.d("AuthRepository", "Access token: ${accessToken.take(20)}...")
            
            // Validate the access token format
            if (!accessToken.startsWith("eyJ")) {
                Log.e("AuthRepository", "Invalid access token format - should be JWT starting with 'eyJ'")
                return Result.failure(Exception("Invalid confirmation token format"))
            }
            
            // Use retrieveUser to confirm the email and establish session
            Log.d("AuthRepository", "Confirming email with access token...")
            SupabaseClient.client.auth.retrieveUser(accessToken)
            
            // Get the current user after email confirmation
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            if (currentUser != null) {
                val fullName = currentUser.userMetadata?.get("full_name")?.toString() ?: ""
                val user = User(
                    id = currentUser.id,
                    name = fullName,
                    username = currentUser.email?.substringBefore("@") ?: "",
                    photoUrl = "",
                    email = currentUser.email ?: ""
                )
                
                // Check if email is confirmed
                val isEmailConfirmed = currentUser.emailConfirmedAt != null
                Log.d("AuthRepository", "Email confirmation status: $isEmailConfirmed")
                Log.d("AuthRepository", "Email confirmed successfully for user: $user")
                
                Result.success(user)
            } else {
                Log.e("AuthRepository", "Email confirmation failed - currentUser is null")
                Result.failure(Exception("Email confirmation failed - unable to retrieve user"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception during email confirmation: ${e.message}", e)
            Result.failure(e)
        }
    }
}
