package com.example.vetconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetconnect.data.model.*
import com.example.vetconnect.data.repository.AppointmentRepository
import com.example.vetconnect.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {
    
    private val _appointmentsState = MutableStateFlow<UiState<List<Appointment>>>(UiState.Loading)
    val appointmentsState: StateFlow<UiState<List<Appointment>>> = _appointmentsState.asStateFlow()
    
    private val _appointmentState = MutableStateFlow<UiState<Appointment>>(UiState.Loading)
    val appointmentState: StateFlow<UiState<Appointment>> = _appointmentState.asStateFlow()
    
    private val _createAppointmentState = MutableStateFlow<UiState<Appointment>>(UiState.Success(null))
    val createAppointmentState: StateFlow<UiState<Appointment>> = _createAppointmentState.asStateFlow()
    
    private val _updateAppointmentState = MutableStateFlow<UiState<Appointment>>(UiState.Success(null))
    val updateAppointmentState: StateFlow<UiState<Appointment>> = _updateAppointmentState.asStateFlow()
    
    private val _deleteAppointmentState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val deleteAppointmentState: StateFlow<UiState<Unit>> = _deleteAppointmentState.asStateFlow()
    
    private val _upcomingAppointmentsState = MutableStateFlow<UiState<List<Appointment>>>(UiState.Loading)
    val upcomingAppointmentsState: StateFlow<UiState<List<Appointment>>> = _upcomingAppointmentsState.asStateFlow()
    
    fun loadAppointments(
        ownerId: String? = null,
        status: AppointmentStatus? = null,
        type: AppointmentType? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        _appointmentsState.value = UiState.Loading
        
        viewModelScope.launch {
            appointmentRepository.getAppointments(
                ownerId = ownerId,
                status = status,
                type = type,
                dateFrom = dateFrom,
                dateTo = dateTo
            ).collect { result ->
                _appointmentsState.value = when {
                    result.isSuccess -> UiState.Success(result.getOrNull() ?: emptyList())
                    result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load appointments")
                }
            }
        }
    }
    
    fun loadUpcomingAppointments(date: String) {
        _upcomingAppointmentsState.value = UiState.Loading
        
        viewModelScope.launch {
            appointmentRepository.getUpcomingAppointments(date).collect { result ->
                _upcomingAppointmentsState.value = when {
                    result.isSuccess -> UiState.Success(result.getOrNull() ?: emptyList())
                    result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load upcoming appointments")
                }
            }
        }
    }
    
    fun loadAppointment(appointmentId: String) {
        _appointmentState.value = UiState.Loading
        
        viewModelScope.launch {
            val result = appointmentRepository.getAppointmentById(appointmentId)
            _appointmentState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load appointment")
            }
        }
    }
    
    fun createAppointment(
        petId: String,
        date: String,
        time: String,
        type: AppointmentType,
        reason: String,
        notes: String? = null
    ) {
        if (petId.isBlank() || date.isBlank() || time.isBlank() || reason.isBlank()) {
            _createAppointmentState.value = UiState.Error("All required fields must be filled")
            return
        }
        
        _createAppointmentState.value = UiState.Loading
        
        viewModelScope.launch {
            val request = CreateAppointmentRequest(
                petId = petId,
                date = date,
                time = time,
                type = type,
                reason = reason,
                notes = notes
            )
            
            val result = appointmentRepository.createAppointment(request)
            _createAppointmentState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create appointment")
            }
        }
    }
    
    fun updateAppointment(
        appointmentId: String,
        date: String? = null,
        time: String? = null,
        type: AppointmentType? = null,
        status: AppointmentStatus? = null,
        reason: String? = null,
        notes: String? = null,
        veterinarianId: String? = null
    ) {
        _updateAppointmentState.value = UiState.Loading
        
        viewModelScope.launch {
            val request = UpdateAppointmentRequest(
                date = date,
                time = time,
                type = type,
                status = status,
                reason = reason,
                notes = notes,
                veterinarianId = veterinarianId
            )
            
            val result = appointmentRepository.updateAppointment(appointmentId, request)
            _updateAppointmentState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update appointment")
            }
        }
    }
    
    fun deleteAppointment(appointmentId: String) {
        _deleteAppointmentState.value = UiState.Loading
        
        viewModelScope.launch {
            val result = appointmentRepository.deleteAppointment(appointmentId)
            _deleteAppointmentState.value = when {
                result.isSuccess -> UiState.Success(Unit)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete appointment")
            }
        }
    }
    
    fun refreshAppointments(
        ownerId: String? = null,
        status: AppointmentStatus? = null,
        type: AppointmentType? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        viewModelScope.launch {
            appointmentRepository.refreshAppointments(
                ownerId = ownerId,
                status = status,
                type = type,
                dateFrom = dateFrom,
                dateTo = dateTo
            )
        }
    }
    
    fun resetCreateAppointmentState() {
        _createAppointmentState.value = UiState.Success(null)
    }
    
    fun resetUpdateAppointmentState() {
        _updateAppointmentState.value = UiState.Success(null)
    }
    
    fun resetDeleteAppointmentState() {
        _deleteAppointmentState.value = UiState.Success(Unit)
    }
} 