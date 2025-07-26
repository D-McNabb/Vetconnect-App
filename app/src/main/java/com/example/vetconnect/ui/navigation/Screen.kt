package com.example.vetconnect.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{token}") {
        val arguments = listOf(
            navArgument("token") { type = NavType.StringType }
        )
    }
    
    // Main Screens
    object Dashboard : Screen("dashboard")
    
    // Pet Screens
    object PetList : Screen("pets")
    object PetDetail : Screen("pets/{petId}") {
        val arguments = listOf(
            navArgument("petId") { type = NavType.StringType }
        )
    }
    object CreatePet : Screen("pets/create")
    object EditPet : Screen("pets/{petId}/edit") {
        val arguments = listOf(
            navArgument("petId") { type = NavType.StringType }
        )
    }
    
    // Appointment Screens
    object AppointmentList : Screen("appointments")
    object AppointmentDetail : Screen("appointments/{appointmentId}") {
        val arguments = listOf(
            navArgument("appointmentId") { type = NavType.StringType }
        )
    }
    object CreateAppointment : Screen("appointments/create")
    object EditAppointment : Screen("appointments/{appointmentId}/edit") {
        val arguments = listOf(
            navArgument("appointmentId") { type = NavType.StringType }
        )
    }
    
    // Admin Screens
    object AdminPanel : Screen("admin")
} 