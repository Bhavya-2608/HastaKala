package com.example.hastakala.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    
    // Using StateFlow values for pre-filling
    val savedEmail by viewModel.userEmail.collectAsState()
    val savedPassword by viewModel.userPassword.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Update local state when saved details change (e.g. after signup/save)
    LaunchedEffect(savedEmail, savedPassword) {
        email = savedEmail
        password = savedPassword
    }

    val error by viewModel.error.collectAsState()
    val showNotFound by viewModel.showUserNotFoundDialog.collectAsState()
    val saveDetailsPair by viewModel.showSaveCredentialsDialog.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF5)) // Artisan Paper
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Welcome" else "Create Account",
            style = MaterialTheme.typography.displaySmall,
            color = Color(0xFFBC4A3C) // ArtTerracotta
        )
        
        Text(
            text = if (isLogin) "Log in to your artisan workspace" else "Join our community of crafters",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2D1F1A).copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (!isLogin) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBC4A3C),
                    unfocusedBorderColor = Color(0xFF2D1F1A).copy(alpha = 0.4f)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBC4A3C),
                unfocusedBorderColor = Color(0xFF2D1F1A).copy(alpha = 0.2f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBC4A3C),
                unfocusedBorderColor = Color(0xFF2D1F1A).copy(alpha = 0.4f)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isLogin) {
                    viewModel.login(email, password, onAuthSuccess)
                } else {
                    viewModel.signup(username, email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1F1A)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                if (isLogin) "LOG IN" else "SIGN UP",
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(
                text = if (isLogin) "New here? Create an account" else "Already have an account? Log in",
                color = Color(0xFFBC4A3C)
            )
        }
    }

    // Dialog: User not found
    if (showNotFound) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUserNotFoundDialog() },
            title = { Text("Account Not Found") },
            text = { Text("We couldn't find an account with this email. Please sign up to create one.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissUserNotFoundDialog()
                        isLogin = false // Navigate to signup
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC4A3C))
                ) {
                    Text("Go to Sign Up")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUserNotFoundDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Save Credentials
    if (saveDetailsPair != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSaveCredentialsDialog() },
            title = { Text("Sign Up Successful!") },
            text = { Text("Would you like to save your artisan account details for easier login?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmSaveCredentials(saveDetailsPair!!.first, saveDetailsPair!!.second)
                        isLogin = true // Navigate directly back to login
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC4A3C))
                ) {
                    Text("Save & Log In")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.dismissSaveCredentialsDialog()
                    isLogin = true // Still navigate back to login but don't save
                }) {
                    Text("Not Now")
                }
            }
        )
    }
}
