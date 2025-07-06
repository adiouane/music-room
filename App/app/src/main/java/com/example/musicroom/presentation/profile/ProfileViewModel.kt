package com.example.musicroom.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.UserProfile
import com.example.musicroom.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            userProfileRepository.getUserProfile()
                .onSuccess { userProfile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = userProfile,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load profile"
                    )
                }
        }
    }
    
    fun updateProfile(
        name: String? = null,
        bio: String? = null,
        dateOfBirth: String? = null,
        phoneNumber: String? = null,
        profilePrivacy: String? = null,
        emailPrivacy: String? = null,
        phonePrivacy: String? = null,
        musicPreferences: List<String>? = null,
        likedArtists: List<String>? = null,
        likedAlbums: List<String>? = null,
        likedSongs: List<String>? = null,
        genres: List<String>? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            
            userProfileRepository.updateUserProfile(
                name = name,
                bio = bio,
                dateOfBirth = dateOfBirth,
                phoneNumber = phoneNumber,
                profilePrivacy = profilePrivacy,
                emailPrivacy = emailPrivacy,
                phonePrivacy = phonePrivacy,
                musicPreferences = musicPreferences,
                likedArtists = likedArtists,
                likedAlbums = likedAlbums,
                likedSongs = likedSongs,
                genres = genres
            )
                .onSuccess { userProfile ->
                    _uiState.value = _uiState.value.copy(
                        userProfile = userProfile,
                        isUpdating = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = error.message ?: "Failed to update profile"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)