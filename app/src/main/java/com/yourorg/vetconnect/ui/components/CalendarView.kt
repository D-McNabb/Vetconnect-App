package com.yourorg.vetconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourorg.vetconnect.model.Appointment
import com.yourorg.vetconnect.model.AppointmentStatus
import com.yourorg.vetconnect.model.AvailableTimeSlot
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

enum class CalendarViewMode {
    MONTH, WEEK, DAY
}

@Composable
fun CalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    appointments: Map<LocalDate, List<Appointment>> = emptyMap(),
    availableSlots: List<AvailableTimeSlot> = emptyList(),
    onSlotSelected: (AvailableTimeSlot) -> Unit = {},
    viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    onViewModeChange: (CalendarViewMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Column(modifier = modifier) {
        // Calendar Header
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            viewMode = viewMode,
            onViewModeChange = onViewModeChange
        )

        when (viewMode) {
            CalendarViewMode.MONTH -> {
                MonthCalendarView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    appointments = appointments
                )
            }
            CalendarViewMode.WEEK -> {
                WeekCalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    appointments = appointments,
                    availableSlots = availableSlots,
                    onSlotSelected = onSlotSelected
                )
            }
            CalendarViewMode.DAY -> {
                DayCalendarView(
                    selectedDate = selectedDate,
                    appointments = appointments[selectedDate] ?: emptyList(),
                    availableSlots = availableSlots,
                    onSlotSelected = onSlotSelected
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // View mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarViewMode.values().forEach { mode ->
                    FilterChip(
                        onClick = { onViewModeChange(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        selected = viewMode == mode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    appointments: Map<LocalDate, List<Appointment>>
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Add empty cells for days before the first day of the month
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Add all days of the month
    repeat(daysInMonth) { day ->
        calendarDays.add(currentMonth.atDay(day + 1))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarDays) { date ->
                    CalendarDayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        hasAppointments = date?.let { appointments[it]?.isNotEmpty() } == true,
                        appointmentCount = date?.let { appointments[it]?.size } ?: 0,
                        onDateSelected = { date?.let { onDateSelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    hasAppointments: Boolean,
    appointmentCount: Int,
    onDateSelected: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        date == null -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = date != null) { onDateSelected() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (date != null) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                )
                
                if (hasAppointments) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.error
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    appointments: Map<LocalDate, List<Appointment>>,
    availableSlots: List<AvailableTimeSlot>,
    onSlotSelected: (AvailableTimeSlot) -> Unit
) {
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() % 7)
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Week header
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { date ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDateSelected(date) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (date == selectedDate) FontWeight.Bold else FontWeight.Normal,
                            color = if (date == selectedDate) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        
                        val dayAppointments = appointments[date]?.size ?: 0
                        if (dayAppointments > 0) {
                            Text(
                                text = "$dayAppointments apt${if (dayAppointments > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Time slots for selected day
            if (availableSlots.isNotEmpty()) {
                Text(
                    text = "Available Times for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableSlots.filter { !it.isBooked }) { slot ->
                        TimeSlotCard(
                            slot = slot,
                            onSlotSelected = { onSlotSelected(slot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCalendarView(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    availableSlots: List<AvailableTimeSlot>,
    onSlotSelected: (AvailableTimeSlot) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Existing appointments
            if (appointments.isNotEmpty()) {
                Text(
                    text = "Scheduled Appointments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                appointments.forEach { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Available time slots
            if (availableSlots.isNotEmpty()) {
                Text(
                    text = "Available Time Slots",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSlots.filter { !it.isBooked }) { slot ->
                        TimeSlotCard(
                            slot = slot,
                            onSlotSelected = { onSlotSelected(slot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSlotCard(
    slot: AvailableTimeSlot,
    onSlotSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSlotSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isBooked) 
                MaterialTheme.colorScheme.surfaceVariant 
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = slot.veterinarianName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!slot.isBooked) {
                FilledTonalButton(
                    onClick = onSlotSelected
                ) {
                    Text("Book")
                }
            } else {
                Text(
                    text = "Booked",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (appointment.status) {
                AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${appointment.startTime} - ${appointment.endTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = appointment.reason,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = appointment.appointmentType.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = appointment.status.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = when (appointment.status) {
                    AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primary
                    AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
