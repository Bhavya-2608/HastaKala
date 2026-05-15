package com.example.hastakala.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hastakala.data.AuthManager
import com.example.hastakala.data.User
import com.example.hastakala.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager,
    private val repository: SaleRepository
) : ViewModel() {
    
    private val _isLoggedIn = MutableStateFlow(authManager.isLoggedIn())
    val isLoggedIn = _isLoggedIn.asStateFlow()

    val username = MutableStateFlow(authManager.getUsername() ?: "")
    val userEmail = MutableStateFlow(authManager.getUserEmail() ?: "")
    val userPassword = MutableStateFlow(authManager.getPassword() ?: "")

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _showUserNotFoundDialog = MutableStateFlow(false)
    val showUserNotFoundDialog = _showUserNotFoundDialog.asStateFlow()

    private val _showSaveCredentialsDialog = MutableStateFlow<Pair<String, String>?>(null)
    val showSaveCredentialsDialog = _showSaveCredentialsDialog.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                if (user.password == password.hashCode()) {
                    authManager.setLoggedIn(true)
                    authManager.saveUserEmail(email)
                    userEmail.value = email
                    authManager.saveUsername(user.username)
                    username.value = user.username
                    // Password saving is usually done on signup or explicit "save" but 
                    // user asked for it to be provided on login if saved.
                    _isLoggedIn.value = true
                    _error.value = null
                    onSuccess()
                } else {
                    _error.value = "Incorrect password"
                }
            } else {
                _showUserNotFoundDialog.value = true
            }
        }
    }

    fun signup(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Invalid email address"
            return
        }

        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            try {
                val existingUser = repository.getUserByEmail(email)
                if (existingUser != null) {
                    _error.value = "Email already registered"
                } else {
                    val newUser = User(email, username, password.hashCode())
                    repository.registerUser(newUser)
                    
                    // Instead of immediate login, show "Save Details" dialog
                    _showSaveCredentialsDialog.value = email to password
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Registration failed: ${e.message}"
            }
        }
    }

    fun confirmSaveCredentials(email: String, password: String) {
        authManager.saveUserEmail(email)
        authManager.savePassword(password)
        userEmail.value = email
        userPassword.value = password
        _showSaveCredentialsDialog.value = null
    }

    fun updateUsername(newName: String) {
        val email = userEmail.value
        if (email.isNotEmpty() && newName.isNotBlank()) {
            viewModelScope.launch {
                repository.updateUsername(email, newName)
                authManager.saveUsername(newName)
                username.value = newName
            }
        }
    }

    fun dismissUserNotFoundDialog() {
        _showUserNotFoundDialog.value = false
    }

    fun dismissSaveCredentialsDialog() {
        _showSaveCredentialsDialog.value = null
    }

    fun logout() {
        authManager.clearSession()
        _isLoggedIn.value = false
        username.value = ""
        userEmail.value = ""
        userPassword.value = ""
    }

    fun deleteAccount() {
        val email = userEmail.value
        if (email.isNotEmpty()) {
            viewModelScope.launch {
                repository.deleteUser(email)
                logout()
            }
        }
    }
}

class AuthViewModelFactory(
    private val authManager: AuthManager,
    private val repository: SaleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authManager, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
