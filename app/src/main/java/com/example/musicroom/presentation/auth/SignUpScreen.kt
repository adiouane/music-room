package com.example.musicroom.presentation.auth

import android.util.Patterns
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.musicroom.presentation.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SignUpScreen(    onSignUpClick: (name: String, email: String, password: String) -> Unit,
    onBackToLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
      // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignUpSuccess -> {
                showSuccessMessage = true
                // The AuthContainer will handle navigation back to login
            }
            is AuthState.Error -> {
                showSuccessMessage = false
            }
            else -> {
                showSuccessMessage = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onboardingGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
              CustomTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = "" // Clear error when user types
                },
                label = "Full Name",
                icon = Icons.Default.Person,
                errorMessage = nameError
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CustomTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = "" // Clear error when user types
                },
                label = "Email",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                errorMessage = emailError
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CustomTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = "" // Clear error when user types
                },
                label = "Password",
                icon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                errorMessage = passwordError,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }                }
            )

            PasswordStrengthIndicator(password)            // Show success message
            if (showSuccessMessage) {
                val currentState = authState
                if (currentState is AuthState.SignUpSuccess) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
              // Show error message if there's an authentication error
            if (authState is AuthState.Error) {
                val errorState = authState as AuthState.Error
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    // Clear previous errors
                    nameError = ""
                    emailError = ""
                    passwordError = ""
                    
                    // Validate inputs
                    var isValid = true
                    
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        isValid = false
                    }
                    
                    if (email.isBlank()) {
                        emailError = "Email is required"
                        isValid = false
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Please enter a valid email"
                        isValid = false
                    }
                    
                    if (password.isBlank()) {
                        passwordError = "Password is required"
                        isValid = false
                    } else if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        isValid = false
                    }
                    
                    if (isValid) {
                        viewModel.signUp(email, password, name)
                    }
                },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign Up")
                }
            }
            
            TextButton(onClick = onBackToLoginClick) {
                Text("Already have an account? Log in")
            }
        }
    }
}

@Composable
private fun SignUpHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Welcome to MusicRoom",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Create an account to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    val strength = when {
        password.length > 8 && password.any { it.isDigit() } && 
        password.any { it.isUpperCase() } -> 3
        password.length > 6 -> 2
        password.isNotEmpty() -> 1
        else -> 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(
                    color = if (index < strength) PrimaryPurple 
                           else TextSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(2.dp)
                )
            )
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String = "",
    isError: Boolean = errorMessage.isNotEmpty()
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = trailingIcon,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = TextSecondary,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
