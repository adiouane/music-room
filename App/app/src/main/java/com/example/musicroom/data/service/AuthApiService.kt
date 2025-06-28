package com.example.musicroom.data.service

import android.util.Log
import com.example.musicroom.data.network.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

/**
 * ========================================================================================
 * AUTHENTICATION API SERVICE - READY FOR BACKEND INTEGRATION
 * ========================================================================================
 * 
 * This service provides complete authentication functionality with real API logic.
 * Currently using MOCK DATA for testing, but ready for backend integration.
 * 
 * 🚀 BACKEND INTEGRATION STEPS:
 * ========================================================================================
 * 1. Replace "YOUR_BACKEND_URL" constant with your actual backend URL
 * 2. Uncomment the real API call methods in each authentication function
 * 3. Remove the mock delay() and mock response generation code
 * 4. Test with your backend endpoints and adjust error handling as needed
 * 
 * 📡 BACKEND ENDPOINTS EXPECTED:
 * ========================================================================================
 * POST /auth/login        - Email/Password login
 * POST /auth/signup       - User registration  
 * POST /auth/forgot-password - Password reset request
 * POST /auth/google       - Google OAuth authentication
 * 
 * 🧪 MOCK DATA TESTING:
 * ========================================================================================
 * Login Success: test@example.com / password123
 * Login Failure: fail@example.com / any password
 * SignUp Success: Any valid email/password/name
 * Google Sign-In: Always succeeds with mock data
 * Forgot Password: Always succeeds with mock response
 * ========================================================================================
 */

// ========================================================================================
// DATA CLASSES - Request/Response models for authentication
// ========================================================================================

/**
 * Login request payload
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Sign up request payload  
 */
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val avatar: String? = null
)

/**
 * Forgot password request payload
 */
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Google Sign-In request payload
 */
data class GoogleSignInRequest(
    val idToken: String,
    val accessToken: String? = null
)

/**
 * Login response model matching backend API
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserInfo? = null,
    // Add any additional fields your backend returns on success
    val refresh_token: String? = null,
    val expires_in: Long? = null
)

/**
 * Sign up response model
 */
data class SignUpResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserInfo? = null
)

/**
 * Forgot password response model
 */
data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

/**
 * Google Sign-In response model
 */
data class GoogleSignInResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserInfo? = null
)

/**
 * User information model
 */
data class UserInfo(
    val id: String,
    val email: String,
    val name: String,
    val username: String? = null,
    val avatar: String? = null
)

// ========================================================================================
// AUTHENTICATION API SERVICE CLASS
// ========================================================================================

@Singleton
class AuthApiService @Inject constructor() {
    
    /**
     * SIGNUP API CALL TO BACKEND (Codespaces compatible)
     */
    suspend fun signUp(email: String, password: String, name: String): Result<SignUpResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthAPI", "📝 Attempting signup for: $email")
                Log.d("AuthAPI", "🌐 Using base URL: ${NetworkConfig.getCurrentBaseUrl()}")
                Log.d("AuthAPI", "🚀 Deployment: ${NetworkConfig.getDeploymentType()}")
                
                // Create the signup request matching your backend API
                val requestBody = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("password", password)
                }
                
                // Use NetworkConfig for URL construction
                val fullUrl = NetworkConfig.getFullUrl(NetworkConfig.Endpoints.SIGNUP)
                Log.d("AuthAPI", "📡 Full URL: $fullUrl")
                
                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                // Handle HTTPS for Codespaces
                if (connection is HttpsURLConnection) {
                    Log.d("AuthAPI", "🔒 Using HTTPS connection")
                }
                
                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "MusicRoom-Android-App")
                    
                    // Add CORS headers for Codespaces
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", "https://solid-train-jwxwrj54vrg25rv9-8000.app.github.dev")
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                Log.d("AuthAPI", "📤 Sending request body: ${requestBody.toString()}")
                
                // Send the request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                Log.d("AuthAPI", "📨 Response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("AuthAPI", "📨 Response body: $responseText")
                
                val response = when (responseCode) {
                    201 -> {
                        // Parse successful response
                        try {
                            val jsonResponse = JSONObject(responseText)
                            SignUpResponse(
                                success = true,
                                message = "Account created successfully",
                                token = jsonResponse.optString("token", null),
                                user = UserInfo(
                                    id = jsonResponse.optString("id", ""),
                                    email = jsonResponse.optString("email", email),
                                    name = jsonResponse.optString("name", name)
                                )
                            )
                        } catch (e: Exception) {
                            Log.w("AuthAPI", "Could not parse success response as JSON, treating as success")
                            SignUpResponse(
                                success = true,
                                message = "Account created successfully",
                                token = null,
                                user = UserInfo(id = "", email = email, name = name)
                            )
                        }
                    }
                    400 -> {
                        // Parse error response
                        val errorMessage = try {
                            val jsonResponse = JSONObject(responseText)
                            // Try different possible error message fields
                            jsonResponse.optString("message") 
                                ?: jsonResponse.optString("error")
                                ?: jsonResponse.optString("detail")
                                ?: "Invalid input"
                        } catch (e: Exception) {
                            "Invalid input data"
                        }
                        SignUpResponse(
                            success = false,
                            message = errorMessage
                        )
                    }
                    404 -> {
                        SignUpResponse(
                            success = false,
                            message = "API endpoint not found. Please check if backend is running."
                        )
                    }
                    500 -> {
                        SignUpResponse(
                            success = false,
                            message = "Server error. Please try again later."
                        )
                    }
                    else -> {
                        SignUpResponse(
                            success = false,
                            message = "Unexpected error occurred (Code: $responseCode)"
                        )
                    }
                }
                
                Log.d("AuthAPI", "✅ SignUp final response: success=${response.success}, message=${response.message}")
                Result.success(response)
                
            } catch (e: Exception) {
                Log.e("AuthAPI", "❌ SignUp error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * LOGIN API CALL TO BACKEND (Codespaces compatible)
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthAPI", "🔐 Attempting login for: $email")
                Log.d("AuthAPI", "🌐 Using base URL: ${NetworkConfig.getCurrentBaseUrl()}")
                
                val requestBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                
                val fullUrl = NetworkConfig.getFullUrl(NetworkConfig.Endpoints.LOGIN)
                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "MusicRoom-Android-App")
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                // Send the request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("AuthAPI", "Login response code: $responseCode")
                Log.d("AuthAPI", "Login response: $responseText")
                
                val response = when (responseCode) {
                    200 -> {
                        try {
                            val jsonResponse = JSONObject(responseText)
                            LoginResponse(
                                success = true,
                                message = "Login successful",
                                token = jsonResponse.optString("token"),
                                user = UserInfo(
                                    id = jsonResponse.optString("id", ""),
                                    email = jsonResponse.optString("email", email),
                                    name = jsonResponse.optString("name", ""),
                                    username = jsonResponse.optString("username"),
                                    avatar = jsonResponse.optString("avatar")
                            ),
                            refresh_token = jsonResponse.optString("refresh_token"),
                            expires_in = jsonResponse.optLong("expires_in")
                        )
                        } catch (e: Exception) {
                            Log.e("AuthAPI", "Error parsing login response", e)
                            LoginResponse(
                                success = true,
                                message = "Login successful",
                                token = null,
                                user = UserInfo(id = "", email = email, name = "")
                            )
                        }
                    }
                    400 -> {
                        LoginResponse(
                            success = false,
                            message = "Missing credentials"
                        )
                    }
                    401 -> {
                        LoginResponse(
                            success = false,
                            message = "Invalid credentials"
                        )
                    }
                    404 -> {
                        LoginResponse(
                            success = false,
                            message = "User not found"
                        )
                    }
                    else -> {
                        LoginResponse(
                            success = false,
                            message = "Server error occurred (Code: $responseCode)"
                        )
                    }
                }
                
                Result.success(response)
                
            } catch (e: Exception) {
                Log.e("AuthAPI", "❌ Login error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * ========================================================================
     * FORGOT PASSWORD API
     * ========================================================================
     * 
     * Sends password reset email to user.
     * ========================================================================
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthAPI", "🔑 Sending password reset for: $email")
                
                // Mock data for testing - replace with real API call
                // Mock success response
                val response = ForgotPasswordResponse(
                    success = true,
                    message = "Password reset email sent to $email"
                )
                
                Log.d("AuthAPI", "✅ Password reset response: ${response.success}")
                Result.success(response)
                
            } catch (e: Exception) {
                Log.e("AuthAPI", "❌ Password reset error: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * ========================================================================
     * GOOGLE SIGN-IN API
     * ========================================================================
     * 
     * Authenticates user with Google OAuth credentials.
     * ========================================================================
     */
    suspend fun signInWithGoogle(idToken: String, accessToken: String? = null): Result<GoogleSignInResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthAPI", "🔗 Attempting Google Sign-In")
                
                // Mock data for testing - replace with real API call
                // Mock success response
                val response = GoogleSignInResponse(
                    success = true,
                    message = "Google Sign-In successful",
                    token = "mock_jwt_token_${System.currentTimeMillis()}",
                    user = UserInfo(
                        id = "google_user_${idToken.hashCode()}",
                        email = "google.user@example.com",
                        name = "Google User"
                    )
                )
                
                Log.d("AuthAPI", "✅ Google Sign-In response: ${response.success}")
                Result.success(response)
                
            } catch (e: Exception) {
                Log.e("AuthAPI", "❌ Google Sign-In error: ${e.message}")
                Result.failure(e)
            }
        }
    }
}