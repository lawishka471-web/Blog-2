package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthFormState {
    object Idle : AuthFormState()
    object Loading : AuthFormState()
    data class Success(val user: User, val message: String) : AuthFormState()
    data class Error(val message: String) : AuthFormState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _formState = MutableStateFlow<AuthFormState>(AuthFormState.Idle)
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    fun login(email: String, pass: String) {
        if (email.trim().isEmpty() || pass.trim().isEmpty()) {
            _formState.value = AuthFormState.Error("Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _formState.value = AuthFormState.Loading
            val result = authRepository.login(email.trim(), pass)
            result.onSuccess { user ->
                _formState.value = AuthFormState.Success(user, "Welcome back, ${user.name}!")
            }.onFailure { error ->
                _formState.value = AuthFormState.Error(error.localizedMessage ?: "Invalid login credentials")
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String) {
        if (name.trim().isEmpty() || email.trim().isEmpty() || pass.isEmpty()) {
            _formState.value = AuthFormState.Error("Please fill in all fields")
            return
        }
        if (pass != confirmPass) {
            _formState.value = AuthFormState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _formState.value = AuthFormState.Loading
            val result = authRepository.register(name.trim(), email.trim(), pass, confirmPass)
            result.onSuccess { user ->
                _formState.value = AuthFormState.Success(user, "Account created successfully!")
            }.onFailure { error ->
                _formState.value = AuthFormState.Error(error.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _formState.value = AuthFormState.Idle
    }

    fun resetState() {
        _formState.value = AuthFormState.Idle
    }
}
