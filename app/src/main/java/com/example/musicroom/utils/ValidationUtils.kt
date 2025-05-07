package com.example.musicroom.utils

import android.util.Patterns

/**
 * Utility class for input validation throughout the application
 * 
 * This class contains static methods for validating user inputs like email,
 * password, name fields, etc.
 */
object ValidationUtils {
    
    /**
     * Validates an email address
     * 
     * Checks that the email is not empty and follows a valid email format
     * 
     * @param email The email address to validate
     * @return True if email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validates a password based on complexity requirements
     * 
     * Password must:
     * - Be at least 6 characters long
     * - Contain at least one uppercase letter
     * - Contain at least one digit
     * - Contain at least one special character
     * 
     * @param password The password to validate
     * @return A Pair containing a Boolean (valid or not) and a message explaining requirements
     */
    fun validatePassword(password: String): Pair<Boolean, String> {
        if (password.isBlank()) {
            return Pair(false, "Password cannot be empty")
        }
        
        val hasMinLength = password.length >= 6
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        val isValid = hasMinLength && hasUpperCase && hasDigit && hasSpecialChar
        
        val message = when {
            !hasMinLength -> "Password must be at least 6 characters"
            !hasUpperCase -> "Password must contain at least one uppercase letter"
            !hasDigit -> "Password must contain at least one digit"
            !hasSpecialChar -> "Password must contain at least one special character"
            else -> "Password is valid"
        }
        
        return Pair(isValid, message)
    }
    
    /**
     * Validates a user's name
     * 
     * Checks that the name is not empty and meets length requirements
     * 
     * @param name The user's name to validate
     * @return A Pair containing a Boolean (valid or not) and a message
     */
    fun validateName(name: String): Pair<Boolean, String> {
        if (name.isBlank()) {
            return Pair(false, "Name cannot be empty")
        }
        
        val trimmedName = name.trim()
        return when {
            trimmedName.length < 2 -> Pair(false, "Name must be at least 2 characters")
            trimmedName.length > 50 -> Pair(false, "Name cannot exceed 50 characters")
            else -> Pair(true, "Name is valid")
        }
    }
    
    /**
     * Checks if two passwords match (for password confirmation)
     * 
     * @param password The original password
     * @param confirmPassword The confirmation password to check against
     * @return True if passwords match, false otherwise
     */    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}
