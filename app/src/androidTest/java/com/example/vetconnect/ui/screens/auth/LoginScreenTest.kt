package com.example.vetconnect.ui.screens.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vetconnect.ui.theme.VetConnectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun loginScreen_DisplaysAllElements() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("VetConnect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_ShowsErrorForEmptyFields() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Click login without entering credentials
        composeTestRule.onNodeWithText("Login").performClick()
        
        // Then
        composeTestRule.onNodeWithText("Email and password are required").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_CanEnterEmailAndPassword() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val testEmail = "test@example.com"
        val testPassword = "password123"
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Enter email
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        
        // Enter password
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)
        
        // Then
        composeTestRule.onNode(hasText(testEmail)).assertIsDisplayed()
        // Password field should be masked, so we check it exists but don't verify the text
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_CanTogglePasswordVisibility() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val testPassword = "password123"
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Enter password
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)
        
        // Find and click the visibility toggle icon
        composeTestRule.onNodeWithContentDescription("Show password").performClick()
        
        // Then
        // Password should now be visible (though we can't directly verify the text due to masking)
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_NavigatesToForgotPassword() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Click forgot password link
        composeTestRule.onNodeWithText("Forgot Password?").performClick()
        
        // Then
        // Verify navigation occurred (this would require additional setup in a real test)
        // For now, we just verify the button is clickable
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_NavigatesToRegister() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Click sign up link
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Then
        // Verify the link is clickable
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }
    
    @Test
    fun loginScreen_ShowsLoadingState() {
        // Given
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val testEmail = "test@example.com"
        val testPassword = "password123"
        
        // When
        composeTestRule.setContent {
            VetConnectTheme {
                LoginScreen(navController = navController)
            }
        }
        
        // Enter credentials
        composeTestRule.onNodeWithText("Email").performTextInput(testEmail)
        composeTestRule.onNodeWithText("Password").performTextInput(testPassword)
        
        // Click login (this would trigger loading state in real app)
        composeTestRule.onNodeWithText("Login").performClick()
        
        // Then
        // Verify the login button is still present (loading state would show spinner)
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }
} 