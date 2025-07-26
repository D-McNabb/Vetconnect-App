package com.example.vetconnect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vetconnect.ui.screens.auth.ForgotPasswordScreen
import com.example.vetconnect.ui.screens.auth.LoginScreen
import com.example.vetconnect.ui.screens.auth.RegisterScreen
import com.example.vetconnect.ui.screens.auth.ResetPasswordScreen
import com.example.vetconnect.ui.screens.dashboard.DashboardScreen
import com.example.vetconnect.ui.screens.appointments.AppointmentDetailScreen
import com.example.vetconnect.ui.screens.appointments.AppointmentListScreen
import com.example.vetconnect.ui.screens.appointments.CreateEditAppointmentScreen
import com.example.vetconnect.ui.screens.pets.CreateEditPetScreen
import com.example.vetconnect.ui.screens.pets.PetDetailScreen
import com.example.vetconnect.ui.screens.pets.PetListScreen
import com.example.vetconnect.ui.screens.admin.AdminPanelScreen

@Composable
fun VetConnectNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        
        composable(
            route = Screen.ResetPassword.route,
            arguments = Screen.ResetPassword.arguments
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(
                navController = navController,
                token = token
            )
        }
        
        // Main Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        // Pet Screens
        composable(Screen.PetList.route) {
            PetListScreen(navController = navController)
        }
        
        composable(
            route = Screen.PetDetail.route,
            arguments = Screen.PetDetail.arguments
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetDetailScreen(
                navController = navController,
                petId = petId
            )
        }
        
        composable(Screen.CreatePet.route) {
            CreateEditPetScreen(
                navController = navController,
                petId = null
            )
        }
        
        composable(
            route = Screen.EditPet.route,
            arguments = Screen.EditPet.arguments
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            CreateEditPetScreen(
                navController = navController,
                petId = petId
            )
        }
        
        // Appointment Screens
        composable(Screen.AppointmentList.route) {
            AppointmentListScreen(navController = navController)
        }
        
        composable(
            route = Screen.AppointmentDetail.route,
            arguments = Screen.AppointmentDetail.arguments
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            AppointmentDetailScreen(
                navController = navController,
                appointmentId = appointmentId
            )
        }
        
        composable(Screen.CreateAppointment.route) {
            CreateEditAppointmentScreen(
                navController = navController,
                appointmentId = null
            )
        }
        
        composable(
            route = Screen.EditAppointment.route,
            arguments = Screen.EditAppointment.arguments
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            CreateEditAppointmentScreen(
                navController = navController,
                appointmentId = appointmentId
            )
        }
        
        // Admin Screens
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(navController = navController)
        }
    }
} 