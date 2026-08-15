package com.example.data.repository

import com.example.data.local.TokenManager
import com.example.data.model.LoginRequest
import com.example.data.model.RegisterRequest
import com.example.data.model.User
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(private val tokenManager: TokenManager) {

    private val api = ApiClient.getApi(tokenManager)

    val currentUser: StateFlow<User?> = tokenManager.userState
    val token: StateFlow<String?> = tokenManager.tokenState

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val effectiveToken = body.getEffectiveToken()
                if (effectiveToken != null) {
                    tokenManager.saveToken(effectiveToken)
                    val user = body.user ?: User(id = 1, name = email.substringBefore("@"), email = email)
                    tokenManager.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception(body.message ?: "Invalid authentication token received"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, passwordConfirmation: String): Result<User> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, passwordConfirmation))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val effectiveToken = body.getEffectiveToken()
                if (effectiveToken != null) {
                    tokenManager.saveToken(effectiveToken)
                    val user = body.user ?: User(id = 2, name = name, email = email)
                    tokenManager.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception(body.message ?: "Failed to receive token on registration"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(): Result<User> {
        return try {
            val response = api.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                tokenManager.saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearAuth()
    }
}
