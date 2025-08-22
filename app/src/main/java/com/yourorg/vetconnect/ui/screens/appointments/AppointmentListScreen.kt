package com.yourorg.vetconnect.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.yourorg.vetconnect.model.Appointment
import com.yourorg.vetconnect.model.AppointmentStatus
import com.yourorg.vetconnect.navigation.Screen
import com.yourorg.vetconnect.ui.components.CalendarView
import com.yourorg.vetconnect.ui.components.CalendarViewMode
import com.yourorg.vetconnect.viewmodel.SimpleAppointmentViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: SimpleAppointmentViewModel = viewModel(
        factory = SimpleAppointmentViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showCalendarView by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf<AppointmentStatus?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadUpcomingAppointments()
        } catch (e: Exception) {
            // Handle initialization error gracefully
            android.util.Log.e("AppointmentList", "Failed to load appointments", e)
        }
    }

    // Show success/error messages
    LaunchedEffect(uiState.showSuccessMessage, uiState.error) {
        // Handle snackbar messages here if needed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Appointments", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showCalendarView = !showCalendarView }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar View")
                    }
                    IconButton(onClick = { navController.navigate(Screen.BookAppointment.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Book Appointment")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.BookAppointment.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Book Appointment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showCalendarView) {
                CalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    appointments = uiState.calendarAppointments,
                    availableSlots = uiState.availableSlots,
                    onSlotSelected = { slot ->
                        // Navigate to book appointment with pre-selected time
                        navController.navigate("${Screen.BookAppointment.route}?date=${selectedDate}&time=${slot.startTime}")
                    },
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    uiState.appointments.isEmpty() -> {
                        EmptyAppointmentsState(
                            onBookAppointment = { 
                                navController.navigate(Screen.BookAppointment.route) 
                            }
                        )
                    }
                    else -> {
                        val filteredAppointments = if (filterStatus != null) {
                            uiState.appointments.filter { it.status == filterStatus }
                        } else {
                            uiState.appointments
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredAppointments) { appointment ->
                                AppointmentListItem(
                                    appointment = appointment,
                                    onEdit = { 
                                        navController.navigate("edit_appointment/${appointment.id}")
                                    },
                                    onCancel = { reason ->
                                        viewModel.cancelAppointment(appointment.id, reason)
                                    },
                                    onReschedule = { newDate, newStartTime, newEndTime ->
                                        viewModel.rescheduleAppointment(
                                            appointment.id,
                                            newDate,
                                            newStartTime,
                                            newEndTime
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = filterStatus,
            onFilterSelected = { status ->
                filterStatus = status
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
private fun AppointmentListItem(
    appointment: Appointment,
    onEdit: () -> Unit,
    onCancel: (String) -> Unit,
    onReschedule: (LocalDate, String, String) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (appointment.status) {
                AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                AppointmentStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.reason,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatAppointmentDate(appointment.appointmentDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${appointment.startTime} - ${appointment.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    StatusChip(status = appointment.status)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UrgencyChip(urgency = appointment.urgencyLevel)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = appointment.appointmentType.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            if (appointment.status == AppointmentStatus.SCHEDULED || 
                appointment.status == AppointmentStatus.CONFIRMED) {
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRescheduleDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reschedule")
                    }
                    
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog) {
        CancelAppointmentDialog(
            onConfirm = { reason ->
                onCancel(reason)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    // Reschedule Dialog
    if (showRescheduleDialog) {
        RescheduleAppointmentDialog(
            currentAppointment = appointment,
            onConfirm = { newDate, newStartTime, newEndTime ->
                onReschedule(newDate, newStartTime, newEndTime)
                showRescheduleDialog = false
            },
            onDismiss = { showRescheduleDialog = false }
        )
    }
}

@Composable
private fun StatusChip(status: AppointmentStatus) {
    val (backgroundColor, textColor) = when (status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        AppointmentStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        AppointmentStatus.NO_SHOW -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        AppointmentStatus.RESCHEDULED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = status.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() },
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor
    )
}

@Composable
private fun UrgencyChip(urgency: com.yourorg.vetconnect.model.UrgencyLevel) {
    val (backgroundColor, textColor) = when (urgency) {
        com.yourorg.vetconnect.model.UrgencyLevel.LOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        com.yourorg.vetconnect.model.UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        com.yourorg.vetconnect.model.UrgencyLevel.HIGH -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        com.yourorg.vetconnect.model.UrgencyLevel.EMERGENCY -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Text(
        text = urgency.name.lowercase().replaceFirstChar { it.uppercase() },
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor
    )
}

@Composable
private fun EmptyAppointmentsState(onBookAppointment: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                        Icon(
                    Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Appointments Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Schedule your first appointment to get started with professional pet care.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FilledTonalButton(
            onClick = onBookAppointment
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Book Appointment")
        }
    }
}

@Composable
private fun FilterDialog(
    currentFilter: AppointmentStatus?,
    onFilterSelected: (AppointmentStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Appointments") },
        text = {
            LazyColumn {
                item {
                    FilterOption(
                        text = "All Appointments",
                        selected = currentFilter == null,
                        onClick = { onFilterSelected(null) }
                    )
                }
                
                items(AppointmentStatus.values()) { status ->
                    FilterOption(
                        text = status.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        selected = currentFilter == status,
                        onClick = { onFilterSelected(status) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun FilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun CancelAppointmentDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Appointment") },
        text = {
            Column {
                Text("Please provide a reason for cancellation:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank()
            ) {
                Text("Cancel Appointment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Appointment")
            }
        }
    )
}

@Composable
private fun RescheduleAppointmentDialog(
    currentAppointment: Appointment,
    onConfirm: (LocalDate, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    // This would typically navigate to a more complex reschedule screen
    // For now, showing a simple dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule Appointment") },
        text = {
            Text("This will open the appointment scheduling interface where you can select a new date and time.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun formatAppointmentDate(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
}
