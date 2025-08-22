package com.yourorg.vetconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.repository.AppointmentRepository
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
// import javax.inject.Inject

// @HiltViewModel
class AppointmentViewModel(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _availableSlots = MutableStateFlow<List<AvailableTimeSlot>>(emptyList())
    val availableSlots: StateFlow<List<AvailableTimeSlot>> = _availableSlots.asStateFlow()

    init {
        // Load appointments safely
        viewModelScope.launch {
            try {
                loadUpcomingAppointments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize: ${e.message}"
                )
            }
        }
    }

    fun loadUpcomingAppointments() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                appointmentRepository.getUpcomingAppointments().collect { appointments ->
                    _uiState.value = _uiState.value.copy(
                        appointments = appointments,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load appointments"
                )
            }
        }
    }

    fun loadAppointmentsByOwner(ownerId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                appointmentRepository.getAppointmentsByOwner(ownerId).collect { appointments ->
                    _uiState.value = _uiState.value.copy(
                        appointments = appointments,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load appointments"
                )
            }
        }
    }

    fun loadAppointmentsByVet(vetId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                appointmentRepository.getAppointmentsByVet(vetId).collect { appointments ->
                    _uiState.value = _uiState.value.copy(
                        appointments = appointments,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load appointments"
                )
            }
        }
    }

    fun loadAppointmentsForDateRange(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            try {
                val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                appointmentRepository.getAppointmentsInDateRange(startMillis, endMillis).collect { appointments ->
                    _uiState.value = _uiState.value.copy(
                        appointments = appointments,
                        calendarAppointments = groupAppointmentsByDate(appointments)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load appointments for date range"
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        loadAvailableSlotsForDate(date)
    }

    fun loadAvailableSlotsForDate(
        date: LocalDate,
        vetId: Long = 1, // Default vet ID, should be dynamic
        appointmentType: AppointmentType = AppointmentType.ROUTINE_CHECKUP
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingSlots = true)
                
                val slots = appointmentRepository.getAvailableTimeSlots(vetId, date, appointmentType)
                _availableSlots.value = slots
                
                _uiState.value = _uiState.value.copy(
                    isLoadingSlots = false,
                    availableSlots = slots
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingSlots = false,
                    error = e.message ?: "Failed to load available slots"
                )
            }
        }
    }

    fun createAppointment(
        petId: Long,
        ownerId: Long,
        veterinarianId: Long,
        appointmentType: AppointmentType,
        date: LocalDate,
        startTime: String,
        endTime: String,
        reason: String,
        urgencyLevel: UrgencyLevel = UrgencyLevel.MEDIUM
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingAppointment = true)
                
                val dateInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                val appointment = Appointment(
                    petId = petId,
                    ownerId = ownerId,
                    veterinarianId = veterinarianId,
                    appointmentType = appointmentType,
                    appointmentDate = dateInMillis,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason,
                    urgencyLevel = urgencyLevel
                )
                
                val appointmentId = appointmentRepository.createAppointment(appointment)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingAppointment = false,
                    createdAppointmentId = appointmentId,
                    showSuccessMessage = "Appointment created successfully!"
                )
                
                // Refresh available slots
                loadAvailableSlotsForDate(date, veterinarianId, appointmentType)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingAppointment = false,
                    error = e.message ?: "Failed to create appointment"
                )
            }
        }
    }

    fun cancelAppointment(appointmentId: Long, reason: String) {
        viewModelScope.launch {
            try {
                appointmentRepository.cancelAppointment(appointmentId, reason)
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Appointment cancelled successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to cancel appointment"
                )
            }
        }
    }

    fun rescheduleAppointment(
        appointmentId: Long,
        newDate: LocalDate,
        newStartTime: String,
        newEndTime: String
    ) {
        viewModelScope.launch {
            try {
                val newDateInMillis = newDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                appointmentRepository.rescheduleAppointment(appointmentId, newDateInMillis, newStartTime, newEndTime)
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Appointment rescheduled successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reschedule appointment"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            showSuccessMessage = null
        )
    }

    private fun groupAppointmentsByDate(appointments: List<Appointment>): Map<LocalDate, List<Appointment>> {
        return appointments.groupBy { appointment ->
            java.time.Instant.ofEpochMilli(appointment.appointmentDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }
}

data class AppointmentUiState(
    val appointments: List<Appointment> = emptyList(),
    val calendarAppointments: Map<LocalDate, List<Appointment>> = emptyMap(),
    val availableSlots: List<AvailableTimeSlot> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val isCreatingAppointment: Boolean = false,
    val createdAppointmentId: Long? = null,
    val error: String? = null,
    val showSuccessMessage: String? = null
)
