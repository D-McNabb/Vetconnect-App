package com.example.vetconnect.ui.viewmodel

import com.example.vetconnect.data.model.AuthResponse
import com.example.vetconnect.data.model.User
import com.example.vetconnect.data.model.UserRole
import com.example.vetconnect.data.repository.AuthRepository
import com.example.vetconnect.ui.state.AuthState
import com.example.vetconnect.ui.state.LoginState
import com.example.vetconnect.ui.state.RegisterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        authViewModel = AuthViewModel(authRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `login with valid credentials should emit success state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val mockUser = User(
            id = "1",
            email = email,
            firstName = "John",
            lastName = "Doe",
            role = UserRole.PET_OWNER,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
        val mockAuthResponse = AuthResponse(
            user = mockUser,
            token = "mock-token",
            refreshToken = "mock-refresh-token"
        )
        
        whenever(authRepository.login(email, password))
            .thenReturn(Result.success(mockAuthResponse))
        
        // When
        authViewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val loginState = authViewModel.loginState.value
        assertTrue(loginState is LoginState.Success)
    }
    
    @Test
    fun `login with empty credentials should emit error state`() = runTest {
        // When
        authViewModel.login("", "")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val loginState = authViewModel.loginState.value
        assertTrue(loginState is LoginState.Error)
        assertEquals("Email and password are required", (loginState as LoginState.Error).message)
    }
    
    @Test
    fun `login with invalid credentials should emit error state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        
        whenever(authRepository.login(email, password))
            .thenReturn(Result.failure(Exception("Invalid credentials")))
        
        // When
        authViewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val loginState = authViewModel.loginState.value
        assertTrue(loginState is LoginState.Error)
        assertEquals("Invalid credentials", (loginState as LoginState.Error).message)
    }
    
    @Test
    fun `register with valid data should emit success state`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "password123"
        val firstName = "John"
        val lastName = "Doe"
        val mockUser = User(
            id = "1",
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = UserRole.PET_OWNER,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
        val mockAuthResponse = AuthResponse(
            user = mockUser,
            token = "mock-token",
            refreshToken = "mock-refresh-token"
        )
        
        whenever(authRepository.register(email, password, firstName, lastName, null, null))
            .thenReturn(Result.success(mockAuthResponse))
        
        // When
        authViewModel.register(email, password, confirmPassword, firstName, lastName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val registerState = authViewModel.registerState.value
        assertTrue(registerState is RegisterState.Success)
    }
    
    @Test
    fun `register with mismatched passwords should emit error state`() = runTest {
        // When
        authViewModel.register(
            email = "test@example.com",
            password = "password123",
            confirmPassword = "differentpassword",
            firstName = "John",
            lastName = "Doe"
        )
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val registerState = authViewModel.registerState.value
        assertTrue(registerState is RegisterState.Error)
        assertEquals("Passwords do not match", (registerState as RegisterState.Error).message)
    }
    
    @Test
    fun `register with empty required fields should emit error state`() = runTest {
        // When
        authViewModel.register(
            email = "",
            password = "password123",
            confirmPassword = "password123",
            firstName = "",
            lastName = "Doe"
        )
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val registerState = authViewModel.registerState.value
        assertTrue(registerState is RegisterState.Error)
        assertEquals("Email is required", (registerState as RegisterState.Error).message)
    }
    
    @Test
    fun `auth state should be authenticated when user is logged in`() = runTest {
        // Given
        val mockUser = User(
            id = "1",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            role = UserRole.PET_OWNER,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01"
        )
        
        whenever(authRepository.isLoggedIn).thenReturn(flowOf(true))
        whenever(authRepository.userData).thenReturn(flowOf(mockUser))
        
        // When
        authViewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = authViewModel.authState.value
        assertTrue(authState is AuthState.Authenticated)
    }
    
    @Test
    fun `auth state should be unauthenticated when user is not logged in`() = runTest {
        // Given
        whenever(authRepository.isLoggedIn).thenReturn(flowOf(false))
        whenever(authRepository.userData).thenReturn(flowOf(null))
        
        // When
        authViewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val authState = authViewModel.authState.value
        assertTrue(authState is AuthState.Unauthenticated)
    }
    
    @Test
    fun `reset states should clear error states`() = runTest {
        // Given - Set error states
        authViewModel.login("", "") // This will set error state
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        authViewModel.resetLoginState()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val loginState = authViewModel.loginState.value
        assertTrue(loginState is LoginState.Idle)
    }
} 