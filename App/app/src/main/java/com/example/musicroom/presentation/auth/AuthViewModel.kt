package com.example.musicroom.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.service.AuthApiService
import com.example.musicroom.data.service.LoginResponse
import com.example.musicroom.data.service.SignUpResponse
import com.example.musicroom.data.service.ForgotPasswordResponse
import com.example.musicroom.data.service.GoogleSignInResponse
import com.example.musicroom.data.auth.TokenManager  // Fixed import - changed from data.service to data.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val response: LoginResponse) : AuthState()
    data class SignUpSuccess(val response: SignUpResponse) : AuthState()
    data class ForgotPasswordSuccess(val response: ForgotPasswordResponse) : AuthState()
    data class GoogleSignInSuccess(val response: GoogleSignInResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /**
     * EMAIL/PASSWORD LOGIN
     * Real logic with mock data - ready for backend integration
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "🔐 Starting login for: $email")
                
                val result = authApiService.login(email, password)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()!!
                    if (response.success) {
                        Log.d("AuthViewModel", "✅ Login successful!")
                        
                        // Save tokens for future API calls
                        response.token?.let { token ->
                            tokenManager.saveToken(token)
                        }
                        
                        _authState.value = AuthState.LoginSuccess(response)
                    } else {
                        Log.d("AuthViewModel", "❌ Login failed: ${response.message}")
                        _authState.value = AuthState.Error(response.message)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Network error"
                    Log.e("AuthViewModel", "❌ Login error: $error")
                    _authState.value = AuthState.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Unexpected error: ${e.message}")
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }
    
    /**
     * EMAIL/PASSWORD SIGNUP
     * Real logic with mock data - ready for backend integration
     */
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "📝 Starting signup for: $email")
                
                val result = authApiService.signUp(email, password, name)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()!!
                    if (response.success) {
                        Log.d("AuthViewModel", "✅ Signup successful!")
                        _authState.value = AuthState.SignUpSuccess(response)
                    } else {
                        Log.d("AuthViewModel", "❌ Signup failed: ${response.message}")
                        _authState.value = AuthState.Error(response.message)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Network error"
                    Log.e("AuthViewModel", "❌ Signup error: $error")
                    _authState.value = AuthState.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Unexpected error: ${e.message}")
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }
    
    /**
     * FORGOT PASSWORD
     * Real logic with mock data - ready for backend integration
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "🔄 Starting password reset for: $email")
                
                val result = authApiService.forgotPassword(email)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()!!
                    if (response.success) {
                        Log.d("AuthViewModel", "✅ Password reset email sent!")
                        _authState.value = AuthState.ForgotPasswordSuccess(response)
                    } else {
                        Log.d("AuthViewModel", "❌ Password reset failed: ${response.message}")
                        _authState.value = AuthState.Error(response.message)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Network error"
                    Log.e("AuthViewModel", "❌ Password reset error: $error")
                    _authState.value = AuthState.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Unexpected error: ${e.message}")
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }
    
    /**
     * GOOGLE SIGN-IN
     * Real logic with mock data - ready for backend integration
     * Call this method after successful Google authentication on client side
     */
    fun signInWithGoogle(idToken: String, accessToken: String? = null) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "🔗 Starting Google sign-in")
                
                val result = authApiService.signInWithGoogle(idToken, accessToken)
                
                if (result.isSuccess) {
                    val response = result.getOrNull()!!
                    if (response.success) {
                        Log.d("AuthViewModel", "✅ Google sign-in successful!")
                        _authState.value = AuthState.GoogleSignInSuccess(response)
                    } else {
                        Log.d("AuthViewModel", "❌ Google sign-in failed: ${response.message}")
                        _authState.value = AuthState.Error(response.message)
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Network error"
                    Log.e("AuthViewModel", "❌ Google sign-in error: $error")
                    _authState.value = AuthState.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Unexpected error: ${e.message}")
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }
    
    /**
     * Logout function to clear stored tokens
     */
    fun logout() {
        tokenManager.clearTokens()
        _authState.value = AuthState.Idle
    }
    
    /**
     * Clear the current auth state
     */
    fun clearState() {
        _authState.value = AuthState.Idle
    }
}