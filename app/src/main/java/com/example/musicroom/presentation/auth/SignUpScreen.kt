package com.example.musicroom.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import com.example.musicroom.presentation.auth.components.PasswordTextField

/**
 * SignUp screen - disabled as authentication will be implemented later
 */
@Composable
fun SignUpScreen(
    onSignUpClick: (name: String, email: String, password: String) -> Unit,
    onBackToLoginClick: () -> Unit
) {
    var signUpState by remember { mutableStateOf(SignUpFormState()) }
    

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SignUpHeader()
        
        SignUpForm(
            state = signUpState,
            onStateChange = { signUpState = it }
        )
        

        
        // Information text for test workflow
        Text(
            text = "Note: For this demo, only test@gmail.com / pass123 will work.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        SignUpButton(
            state = signUpState,
            onSignUpClick = onSignUpClick,
            isLoading = false
        )
        
        LoginLink(onBackToLoginClick)
    }
}

@Composable
private fun SignUpHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(40.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ) {}
        }
        
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
            )        }
    }
}

@Composable
private fun SignUpForm(
    state: SignUpFormState,
    onStateChange: (SignUpFormState) -> Unit
) {
    CustomTextField(
        value = state.name,
        onValueChange = { onStateChange(state.copy(name = it)) },
        label = "Full Name",
        icon = Icons.Default.Person
    )
    
    CustomTextField(
        value = state.email,
        onValueChange = { onStateChange(state.copy(email = it)) },
        label = "Email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
    )
    
    var passwordVisible by remember { mutableStateOf(false) }
    CustomTextField(
        value = state.password,
        onValueChange = { onStateChange(state.copy(password = it)) },
        label = "Password",
        icon = Icons.Default.Lock,
        keyboardType = KeyboardType.Password
    )
}


@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun SignUpButton(
    state: SignUpFormState,
    onSignUpClick: (String, String, String) -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = {
            if (state.isValid()) {
                onSignUpClick(state.name, state.email, state.password)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = state.isValid() && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Create Account")
        }
    }
}

@Composable
private fun LoginLink(onBackToLoginClick: () -> Unit) {
    TextButton(onClick = onBackToLoginClick) {
        Text("Already have an account? Log In")
    }
}

private data class SignUpFormState(
    val name: String = "",
    val email: String = "",
    val password: String = ""
) {
    fun isValid() = name.isNotBlank() && 
                    email.isNotBlank() && 
                    password.length >= 6
}
