package com.example.musicroom.data.repositories

import com.example.musicroom.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for handling authentication operations
 * 
 * This interface defines the contract for all authentication operations
 * in the MusicRoom application, including user sign-in, sign-up, and account management.
 */
interface AuthRepository {
    /**
     * Flow that emits the current authenticated user or null if not authenticated
     */
    val currentUser: StateFlow<User?>
    
    /**
     * Signs in a user with email and password
     * 
     * @param email User's email address
     * @param password User's password
     * @return Result containing the authenticated user on success or an exception on failure
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    
    /**
     * Creates a new user account with email and password
     * 
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     * @return Result containing the newly created user on success or an exception on failure
     */
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<User>
    
    /**
     * Signs in a user with Google authentication
     * 
     * @return Result containing the authenticated user on success or an exception on failure
     */
    suspend fun signInWithGoogle(): Result<User>
    
    
    /**
     * Signs out the current user
     * 
     * @return Result indicating success or failure of the operation
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Sends a password reset email to the provided email address
     * 
     * @param email Email address to send the password reset link to
     * @return Result indicating success or failure of the operation
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
      /**
     * Deletes the current user's account
     * 
     * @return Result indicating success or failure of the operation
     */
    suspend fun deleteAccount(): Result<Unit>
}
