package com.yourorg.vetconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.repository.AppointmentRepository
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
// import javax.inject.Inject

// @HiltViewModel
class VetAvailabilityViewModel(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VetAvailabilityUiState())
    val uiState: StateFlow<VetAvailabilityUiState> = _uiState.asStateFlow()

    fun loadVetAvailability(vetId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                appointmentRepository.getVetAvailability(vetId).collect { availabilities ->
                    _uiState.value = _uiState.value.copy(
                        availabilities = availabilities,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load availability"
                )
            }
        }
    }

    fun addAvailability(
        vetId: Long,
        clinicId: Long,
        dayOfWeek: DayOfWeek,
        startTime: String,
        endTime: String,
        appointmentTypes: List<AppointmentType> = AppointmentType.values().toList(),
        slotDuration: Int = 30
    ) {
        viewModelScope.launch {
            try {
                val availability = VetAvailability(
                    veterinarianId = vetId,
                    clinicId = clinicId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    slotDuration = slotDuration,
                    appointmentTypes = appointmentTypes
                )
                
                appointmentRepository.addVetAvailability(availability)
                
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Availability added successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add availability"
                )
            }
        }
    }

    fun updateAvailability(availability: VetAvailability) {
        viewModelScope.launch {
            try {
                appointmentRepository.updateVetAvailability(availability)
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Availability updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update availability"
                )
            }
        }
    }

    fun removeAvailability(availabilityId: Long) {
        viewModelScope.launch {
            try {
                appointmentRepository.removeVetAvailability(availabilityId)
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Availability removed successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove availability"
                )
            }
        }
    }

    fun addBlockedSlot(
        vetId: Long,
        startDateTime: Long,
        endDateTime: Long,
        reason: String,
        isRecurring: Boolean = false,
        recurringPattern: RecurringPattern? = null
    ) {
        viewModelScope.launch {
            try {
                val blockedSlot = BlockedSlot(
                    veterinarianId = vetId,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    reason = reason,
                    isRecurring = isRecurring,
                    recurringPattern = recurringPattern
                )
                
                appointmentRepository.addBlockedSlot(blockedSlot)
                
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Time blocked successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to block time"
                )
            }
        }
    }

    fun generateWeeklySchedule(
        vetId: Long,
        clinicId: Long,
        mondayToFridayHours: Pair<String, String> = "09:00" to "17:00",
        saturdayHours: Pair<String, String>? = "09:00" to "13:00",
        sundayHours: Pair<String, String>? = null
    ) {
        viewModelScope.launch {
            try {
                val availabilities = mutableListOf<VetAvailability>()
                
                // Monday to Friday
                for (day in listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                    availabilities.add(
                        VetAvailability(
                            veterinarianId = vetId,
                            clinicId = clinicId,
                            dayOfWeek = day,
                            startTime = mondayToFridayHours.first,
                            endTime = mondayToFridayHours.second,
                            appointmentTypes = AppointmentType.values().toList()
                        )
                    )
                }
                
                // Saturday
                saturdayHours?.let { (start, end) ->
                    availabilities.add(
                        VetAvailability(
                            veterinarianId = vetId,
                            clinicId = clinicId,
                            dayOfWeek = DayOfWeek.SATURDAY,
                            startTime = start,
                            endTime = end,
                            appointmentTypes = listOf(
                                AppointmentType.ROUTINE_CHECKUP,
                                AppointmentType.VACCINATION
                            )
                        )
                    )
                }
                
                // Sunday
                sundayHours?.let { (start, end) ->
                    availabilities.add(
                        VetAvailability(
                            veterinarianId = vetId,
                            clinicId = clinicId,
                            dayOfWeek = DayOfWeek.SUNDAY,
                            startTime = start,
                            endTime = end,
                            appointmentTypes = listOf(AppointmentType.EMERGENCY)
                        )
                    )
                }
                
                availabilities.forEach { availability ->
                    appointmentRepository.addVetAvailability(availability)
                }
                
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = "Weekly schedule created successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create weekly schedule"
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
}

data class VetAvailabilityUiState(
    val availabilities: List<VetAvailability> = emptyList(),
    val blockedSlots: List<BlockedSlot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null
)
