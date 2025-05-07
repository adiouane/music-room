package com.example.musicroom.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.User
import com.example.musicroom.data.repositories.AuthRepository
import com.example.musicroom.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State class representing the current authentication state
 * 
 * @property isLoading Indicates if an authentication operation is in progress
 * @property user The currently authenticated user, null if not authenticated
 * @property error Any error message that should be displayed to the user
 * @property fieldErrors Map of field validation errors for real-time validation
 */
data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

/**
 * ViewModel responsible for managing authentication state and operations
 * 
 * This ViewModel handles user authentication operations such as sign-in, sign-up,
 * social media authentication, and validation of user inputs.
 * 
 * @property authRepository Repository that handles authentication operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Initialize with the current user from repository
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.update { it.copy(user = user) }
            }
        }
    }

    /**
     * Validates user inputs for authentication
     * 
     * @param email User's email address
     * @param password User's password
     * @param name User's name (optional for sign-in)
     * @return True if all provided inputs are valid, false otherwise
     */
    private fun validateInputs(
        email: String,
        password: String,
        name: String? = null
    ): Boolean {
        val errors = mutableMapOf<String, String>()
        
        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            errors["email"] = "Please enter a valid email address"
        }
        
        // Validate password
        val (isValidPassword, passwordMessage) = ValidationUtils.validatePassword(password)
        if (!isValidPassword) {
            errors["password"] = passwordMessage
        }
        
        // Validate name if provided (for sign-up)
        if (name != null && name.trim().length < 3) {
            errors["name"] = "Name must be at least 3 characters"
        }
        
        // Update state with validation errors
        _authState.update { it.copy(fieldErrors = errors) }
        
        return errors.isEmpty()
    }

    /**
     * Attempts to sign in a user with email and password
     * 
     * @param email User's email address
     * @param password User's password
     */
    fun signInWithEmail(email: String, password: String) {
        if (!validateInputs(email, password)) {
            return
        }
        
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _authState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _authState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Creates a new user account with email, password, and name
     * 
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     */
    fun signUpWithEmail(name: String, email: String, password: String) {
        if (!validateInputs(email, password, name)) {
            return
        }
        
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.signUpWithEmail(name, email, password)
                .onSuccess { user ->
                    _authState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _authState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }    /**
     * Attempts to sign in with Google account
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.signInWithGoogle()
                .onSuccess { user ->
                    _authState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _authState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
    

    /**
     * Signs out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.signOut()
                .onSuccess {
                    _authState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _authState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }
    
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _authState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _authState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }    // This method is redundant since we already have sendPasswordResetEmail
    // Keeping it for backward compatibility
    fun resetPassword(email: String) {
        sendPasswordResetEmail(email)
    }
}