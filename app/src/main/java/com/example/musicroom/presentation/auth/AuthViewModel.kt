package com.example.musicroom.presentation.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.User
import com.example.musicroom.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    fun signUp(email: String, password: String, fullName: String) {
        Log.d("AuthViewModel", "signUp called with email: $email, name: $fullName")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Set state to Loading")
            
            val result = authRepository.signUp(email, password, fullName)
            Log.d("AuthViewModel", "Repository result: ${result.isSuccess}")
            
            _authState.value = if (result.isSuccess) {
                Log.d("AuthViewModel", "Success! User: ${result.getOrNull()}")
                AuthState.Success(result.getOrNull()!!)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Sign up failed"
                Log.e("AuthViewModel", "Error: $error")
                AuthState.Error(error)
            }
        }
    }
      fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }    fun getGoogleSignInIntent(): Intent {
        Log.d("AuthViewModel", "Getting Google Sign-In intent")
        return authRepository.getGoogleSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?) {
        Log.d("AuthViewModel", "Handling Google Sign-In result")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.handleGoogleSignInResult(data)
            _authState.value = if (result.isSuccess) {
                Log.d("AuthViewModel", "Google Sign-In successful")
                AuthState.Success(result.getOrNull()!!)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Google Sign in failed"
                Log.e("AuthViewModel", "Google Sign-In failed: $errorMessage")
                AuthState.Error(errorMessage)
            }
        }
    }
    
    fun clearError() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}