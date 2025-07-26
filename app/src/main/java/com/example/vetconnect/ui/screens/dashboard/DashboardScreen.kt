package com.example.vetconnect.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vetconnect.data.model.Appointment
import com.example.vetconnect.data.model.UserRole
import com.example.vetconnect.ui.components.EmptyStateScreen
import com.example.vetconnect.ui.components.LoadingScreen
import com.example.vetconnect.ui.navigation.Screen
import com.example.vetconnect.ui.theme.VetConnectTheme
import com.example.vetconnect.ui.viewmodel.AuthViewModel
import com.example.vetconnect.ui.viewmodel.AppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    val userData by authViewModel.userData.collectAsState()
    val upcomingAppointmentsState by appointmentViewModel.upcomingAppointmentsState.collectAsState()
    
    LaunchedEffect(Unit) {
        appointmentViewModel.loadUpcomingAppointments(LocalDate.now().toString())
    }
    
    VetConnectTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    actions = {
                        IconButton(onClick = { authViewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Welcome back, ${userData?.firstName ?: "User"}!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Manage your pets and appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Quick Actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Book Appointment Card
                        Card(
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.CreateAppointment.route) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Book Appointment",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Book Appointment",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        
                        // View Pets Card
                        Card(
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Screen.PetList.route) }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "View Pets",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "View Pets",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Admin Panel Card (only for admins)
                if (userData?.role == UserRole.ADMIN) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(Screen.AdminPanel.route) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Admin Panel",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Manage users and view all data",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Upcoming Appointments
                item {
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                when (upcomingAppointmentsState) {
                    is com.example.vetconnect.ui.state.UiState.Loading -> {
                        item {
                            LoadingScreen("Loading appointments...")
                        }
                    }
                    is com.example.vetconnect.ui.state.UiState.Success -> {
                        val appointments = upcomingAppointmentsState.data
                        if (appointments.isEmpty()) {
                            item {
                                EmptyStateScreen(
                                    title = "No Upcoming Appointments",
                                    message = "You don't have any appointments scheduled.",
                                    actionText = "Book Appointment",
                                    onAction = { navController.navigate(Screen.CreateAppointment.route) }
                                )
                            }
                        } else {
                            items(appointments.take(5)) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onClick = {
                                        navController.navigate(Screen.AppointmentDetail.route.replace("{appointmentId}", appointment.id))
                                    }
                                )
                            }
                            
                            if (appointments.size > 5) {
                                item {
                                    TextButton(
                                        text = "View All Appointments",
                                        onClick = { navController.navigate(Screen.AppointmentList.route) }
                                    )
                                }
                            }
                        }
                    }
                    is com.example.vetconnect.ui.state.UiState.Error -> {
                        item {
                            Text(
                                text = upcomingAppointmentsState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (appointment.type) {
                    com.example.vetconnect.data.model.AppointmentType.CHECKUP -> Icons.Default.MedicalServices
                    com.example.vetconnect.data.model.AppointmentType.VACCINATION -> Icons.Default.Vaccines
                    com.example.vetconnect.data.model.AppointmentType.SURGERY -> Icons.Default.LocalHospital
                    com.example.vetconnect.data.model.AppointmentType.EMERGENCY -> Icons.Default.Emergency
                    com.example.vetconnect.data.model.AppointmentType.FOLLOW_UP -> Icons.Default.Refresh
                    com.example.vetconnect.data.model.AppointmentType.CONSULTATION -> Icons.Default.Chat
                },
                contentDescription = appointment.type.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appointment.pet?.name ?: "Unknown Pet",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = appointment.type.name.replace("_", " ").lowercase().capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${appointment.date} at ${appointment.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 