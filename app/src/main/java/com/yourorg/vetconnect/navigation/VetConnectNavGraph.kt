package com.yourorg.vetconnect.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yourorg.vetconnect.ui.screens.auth.LoginScreen
import com.yourorg.vetconnect.ui.screens.auth.RegisterScreen
import com.yourorg.vetconnect.ui.screens.dashboard.DashboardScreen
import com.yourorg.vetconnect.ui.screens.pets.PetListScreen
import com.yourorg.vetconnect.ui.screens.pets.ImprovedAddPetScreen
import com.yourorg.vetconnect.ui.screens.pets.PetDetailScreen
import com.yourorg.vetconnect.ui.screens.appointments.AppointmentListScreen
import com.yourorg.vetconnect.ui.screens.appointments.BookAppointmentScreen
import com.yourorg.vetconnect.ui.screens.appointments.AppointmentCalendarScreen
import com.yourorg.vetconnect.ui.screens.appointments.AppointmentDetailScreen
import com.yourorg.vetconnect.ui.screens.appointments.EditAppointmentScreen
import com.yourorg.vetconnect.ui.screens.health.HealthRecordsScreen
import com.yourorg.vetconnect.ui.screens.health.ImprovedHealthRecordsScreen
import com.yourorg.vetconnect.ui.screens.vet.VetDashboardScreen

@Composable
fun VetConnectNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        // Main Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        // Pet Screens
        composable(Screen.PetList.route) {
            PetListScreen(navController = navController)
        }
        
        composable(Screen.AddPet.route) {
            ImprovedAddPetScreen(navController = navController)
        }

        composable(
            route = Screen.PetDetail.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
            PetDetailScreen(
                navController = navController,
                petId = petId
            )
        }
        
        // Appointment Screens
        composable(Screen.AppointmentList.route) {
            AppointmentListScreen(navController = navController)
        }
        
        composable(Screen.BookAppointment.route) {
            BookAppointmentScreen(navController = navController)
        }
        
        composable(Screen.AppointmentCalendar.route) {
            AppointmentCalendarScreen(navController = navController)
        }
        
        composable(
            route = Screen.AppointmentDetail.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
            AppointmentDetailScreen(
                navController = navController,
                appointmentId = appointmentId
            )
        }
        
        composable(
            route = Screen.EditAppointment.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getLong("appointmentId") ?: 0L
            EditAppointmentScreen(
                navController = navController,
                appointmentId = appointmentId
            )
        }
        
        // Health Records
        composable(
            route = Screen.HealthRecords.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
            ImprovedHealthRecordsScreen(
                navController = navController,
                petId = petId
            )
        }
        
        // Vet Screens
        composable(Screen.VetDashboard.route) {
            VetDashboardScreen(navController = navController)
        }
    }
}
