package com.example.vetconnect.data.repository

import com.example.vetconnect.data.api.VetConnectApiService
import com.example.vetconnect.data.local.AuthDataStore
import com.example.vetconnect.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: VetConnectApiService,
    private val authDataStore: AuthDataStore
) {
    
    val isLoggedIn: Flow<Boolean> = authDataStore.isLoggedIn
    val userData: Flow<User?> = authDataStore.userData
    val accessToken: Flow<String?> = authDataStore.accessToken
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.success && response.data != null) {
                authDataStore.saveAuthData(
                    response.data.token,
                    response.data.refreshToken,
                    response.data.user
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String? = null,
        address: String? = null
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(email, password, firstName, lastName, phone, address)
            )
            if (response.success && response.data != null) {
                authDataStore.saveAuthData(
                    response.data.token,
                    response.data.refreshToken,
                    response.data.user
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(token: String, password: String): Result<Unit> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(token, password))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(): Result<AuthResponse> {
        return try {
            val refreshToken = authDataStore.getRefreshTokenSync()
            if (refreshToken != null) {
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.success && response.data != null) {
                    authDataStore.saveAuthData(
                        response.data.token,
                        response.data.refreshToken,
                        response.data.user
                    )
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            } else {
                Result.failure(Exception("No refresh token available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout()
            authDataStore.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            // Even if API call fails, clear local data
            authDataStore.clearAuthData()
            Result.success(Unit)
        }
    }
    
    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.success && response.data != null) {
                authDataStore.updateUserData(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        address: String? = null
    ): Result<User> {
        return try {
            val response = apiService.updateProfile(
                UpdateProfileRequest(firstName, lastName, phone, address)
            )
            if (response.success && response.data != null) {
                authDataStore.updateUserData(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 