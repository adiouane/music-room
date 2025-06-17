package com.example.musicroom.data.auth

import android.util.Log

object DeepLinkManager {
    private var _accessToken: String? = null
    private var _refreshToken: String? = null
    private var _type: String? = null
    
    fun setPasswordResetTokens(accessToken: String?, refreshToken: String?, type: String?) {
        Log.d("DeepLinkManager", "Setting tokens - Access: ${accessToken?.take(20)}..., Type: $type")
        _accessToken = accessToken
        _refreshToken = refreshToken
        _type = type
    }
    
    fun getPasswordResetTokens(): Triple<String?, String?, String?> {
        Log.d("DeepLinkManager", "Getting tokens - Access: ${_accessToken?.take(20)}..., Type: $_type")
        return Triple(_accessToken, _refreshToken, _type)
    }
    
    fun clearTokens() {
        Log.d("DeepLinkManager", "Clearing tokens")
        _accessToken = null
        _refreshToken = null
        _type = null
    }
}
