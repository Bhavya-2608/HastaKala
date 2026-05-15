package com.example.hastakala.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hastakala_auth", Context.MODE_PRIVATE)

    private val _userEmailFlow = MutableStateFlow(getUserEmail() ?: "")
    val userEmailFlow = _userEmailFlow.asStateFlow()

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
        _userEmailFlow.value = email
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("user_name", username).apply()
    }

    fun getUsername(): String? {
        return prefs.getString("user_name", null)
    }

    fun savePassword(password: String) {
        prefs.edit().putString("user_password", password).apply()
    }

    fun getPassword(): String? {
        return prefs.getString("user_password", null)
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
        _userEmailFlow.value = ""
    }
}
