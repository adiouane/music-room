package com.example.musicroom.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.musicroomi.R

class GoogleAuthUiClient(
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    fun getSignInIntent(): Intent {
        Log.d("GoogleAuthUiClient", "🔗 Creating Google Sign-In intent")
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithIntent(data: Intent?): GoogleSignInResult {
        return try {
            Log.d("GoogleAuthUiClient", "🔑 Processing Google Sign-In result")
            
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account?.idToken == null) {
                Log.e("GoogleAuthUiClient", "❌ Google ID token is null")
                return GoogleSignInResult(
                    data = null,
                    errorMessage = "Failed to get Google ID token"
                )
            }
            
            Log.d("GoogleAuthUiClient", "🔥 Authenticating with Firebase")
            
            // Authenticate with Firebase using the Google ID token
            val firebaseCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Log.e("GoogleAuthUiClient", "❌ Firebase user is null")
                return GoogleSignInResult(
                    data = null,
                    errorMessage = "Failed to authenticate with Firebase"
                )
            }
            
            Log.d("GoogleAuthUiClient", "✅ Google Sign-In successful: ${firebaseUser.displayName}")
            
            val googleUser = GoogleUserInfo(
                userId = firebaseUser.uid,
                username = firebaseUser.displayName,
                profilePictureUrl = firebaseUser.photoUrl?.toString()
            )
            
            GoogleSignInResult(
                data = googleUser,
                errorMessage = null
            )
        } catch (e: ApiException) {
            Log.e("GoogleAuthUiClient", "❌ Google Sign-In failed with code: ${e.statusCode}", e)
            GoogleSignInResult(
                data = null,
                errorMessage = "Google Sign-In failed: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "❌ Google Sign-In error: ${e.message}", e)
            GoogleSignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            auth.signOut()
            Log.d("GoogleAuthUiClient", "✅ Signed out successfully")
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "❌ Sign out failed: ${e.message}", e)
        }
    }

    fun getSignedInUser(): GoogleUserInfo? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { user ->
            GoogleUserInfo(
                userId = user.uid,
                username = user.displayName,
                profilePictureUrl = user.photoUrl?.toString()
            )
        }
    }
}
