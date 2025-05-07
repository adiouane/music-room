package com.example.musicroom.data.repositories

import com.example.musicroom.data.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Static mock implementation of AuthRepository interface
 * 
 * This class provides a mock implementation with static data for authentication,
 * completely independent of any external authentication provider.
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Static mock users
    private val mockUsers = mutableMapOf<String, User>()
    
    init {
        // Initialize with some mock users
        mockUsers["user@example.com"] = User(
            id = "user123",
            name = "Test User",
            email = "user@example.com"
        )
    }
    
    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            delay(500) // Simulate network delay
            
            // Perform basic validation
            if (email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Email and password cannot be empty")
            }
            
            // Check if user exists (in a real app, you would also validate the password)
            val user = mockUsers[email] ?: throw IllegalArgumentException("Invalid email or password")
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<User> {
        return try {
            delay(500) // Simulate network delay
            
            // Perform basic validation
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Name, email and password cannot be empty")
            }
            
            if (name.length < 3) {
                throw IllegalArgumentException("Name must be at least 3 characters")
            }
            
            // Check if user already exists
            if (mockUsers.containsKey(email)) {
                throw IllegalArgumentException("Email already in use")
            }
            
            // Create new user
            val newUser = User(
                id = "user_" + System.currentTimeMillis(),
                name = name,
                email = email
            )
            
            // Store user in mock database
            mockUsers[email] = newUser
            
            _currentUser.value = newUser
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            delay(500) // Simulate network delay
            
            val user = User(
                id = "google123",
                name = "Google User",
                email = "google@example.com",
                photoUrl = "https://example.com/photo.jpg"
            )
            
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            delay(500) // Simulate network delay
            
            if (email.isBlank()) {
                throw IllegalArgumentException("Email cannot be empty")
            }
            
            if (!mockUsers.containsKey(email)) {
                throw IllegalArgumentException("No account found with this email")
            }
            
            // In a real app, you would send an actual email
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUserEmail = _currentUser.value?.email
                ?: throw IllegalStateException("No user is currently signed in")
            
            mockUsers.remove(currentUserEmail)
            _currentUser.value = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
