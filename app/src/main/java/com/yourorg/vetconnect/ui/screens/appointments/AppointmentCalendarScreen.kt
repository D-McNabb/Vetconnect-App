package com.yourorg.vetconnect.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.yourorg.vetconnect.ui.components.CalendarView
import com.yourorg.vetconnect.ui.components.CalendarViewMode
import com.yourorg.vetconnect.viewmodel.SimpleAppointmentViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCalendarScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: SimpleAppointmentViewModel = viewModel(
        factory = SimpleAppointmentViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }

    LaunchedEffect(Unit) {
        viewModel.loadAppointmentsForDateRange(
            selectedDate.withDayOfMonth(1),
            selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Appointment Calendar", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CalendarView(
                selectedDate = selectedDate,
                onDateSelected = { date -> viewModel.selectDate(date) },
                appointments = uiState.calendarAppointments,
                availableSlots = uiState.availableSlots,
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onSlotSelected = { slot ->
                    // Navigate to book appointment
                    navController.navigate("book_appointment")
                },
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
