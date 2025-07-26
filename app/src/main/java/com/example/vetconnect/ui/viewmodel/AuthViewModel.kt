package com.example.vetconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetconnect.data.repository.AuthRepository
import com.example.vetconnect.ui.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()
    
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()
    
    val userData = authRepository.userData.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { isLoggedIn ->
                _authState.value = if (isLoggedIn) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginState.value = when {
                result.isSuccess -> LoginState.Success
                result.isFailure -> LoginState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
    
    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phone: String? = null,
        address: String? = null
    ) {
        when {
            email.isBlank() -> {
                _registerState.value = RegisterState.Error("Email is required")
                return
            }
            password.isBlank() -> {
                _registerState.value = RegisterState.Error("Password is required")
                return
            }
            password != confirmPassword -> {
                _registerState.value = RegisterState.Error("Passwords do not match")
                return
            }
            firstName.isBlank() -> {
                _registerState.value = RegisterState.Error("First name is required")
                return
            }
            lastName.isBlank() -> {
                _registerState.value = RegisterState.Error("Last name is required")
                return
            }
        }
        
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            val result = authRepository.register(email, password, firstName, lastName, phone, address)
            _registerState.value = when {
                result.isSuccess -> RegisterState.Success
                result.isFailure -> RegisterState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
    
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Email is required")
            return
        }
        
        _forgotPasswordState.value = ForgotPasswordState.Loading
        
        viewModelScope.launch {
            val result = authRepository.forgotPassword(email)
            _forgotPasswordState.value = when {
                result.isSuccess -> ForgotPasswordState.Success
                result.isFailure -> ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }
    
    fun resetPassword(token: String, password: String, confirmPassword: String) {
        when {
            token.isBlank() -> {
                _forgotPasswordState.value = ForgotPasswordState.Error("Reset token is required")
                return
            }
            password.isBlank() -> {
                _forgotPasswordState.value = ForgotPasswordState.Error("Password is required")
                return
            }
            password != confirmPassword -> {
                _forgotPasswordState.value = ForgotPasswordState.Error("Passwords do not match")
                return
            }
        }
        
        _forgotPasswordState.value = ForgotPasswordState.Loading
        
        viewModelScope.launch {
            val result = authRepository.resetPassword(token, password)
            _forgotPasswordState.value = when {
                result.isSuccess -> ForgotPasswordState.Success
                result.isFailure -> ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Failed to reset password")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
    
    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
} 