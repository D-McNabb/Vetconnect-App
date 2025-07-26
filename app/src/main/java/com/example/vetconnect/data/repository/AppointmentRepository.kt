package com.example.vetconnect.data.repository

import com.example.vetconnect.data.api.VetConnectApiService
import com.example.vetconnect.data.local.AppointmentDao
import com.example.vetconnect.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val apiService: VetConnectApiService,
    private val appointmentDao: AppointmentDao
) {
    
    fun getAppointments(
        ownerId: String? = null,
        status: AppointmentStatus? = null,
        type: AppointmentType? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Flow<Result<List<Appointment>>> = flow {
        try {
            // First emit cached data
            val cachedAppointments = if (ownerId != null) {
                appointmentDao.getAppointmentsByOwner(ownerId)
            } else {
                appointmentDao.getAllAppointments()
            }
            emit(Result.success(cachedAppointments))
            
            // Then fetch from API
            val response = apiService.getAppointments(
                status = status?.name?.lowercase(),
                type = type?.name?.lowercase(),
                dateFrom = dateFrom,
                dateTo = dateTo,
                ownerId = ownerId
            )
            if (response.success) {
                appointmentDao.insertAppointments(response.data)
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    fun getUpcomingAppointments(date: String): Flow<Result<List<Appointment>>> = flow {
        try {
            val cachedAppointments = appointmentDao.getUpcomingAppointments(date)
            emit(Result.success(cachedAppointments))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment> {
        return try {
            // First try to get from cache
            val cachedAppointment = appointmentDao.getAppointmentById(appointmentId)
            if (cachedAppointment != null) {
                return Result.success(cachedAppointment)
            }
            
            // If not in cache, fetch from API
            val response = apiService.getAppointment(appointmentId)
            if (response.success && response.data != null) {
                appointmentDao.insertAppointment(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createAppointment(request: CreateAppointmentRequest): Result<Appointment> {
        return try {
            val response = apiService.createAppointment(request)
            if (response.success && response.data != null) {
                appointmentDao.insertAppointment(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateAppointment(
        appointmentId: String,
        request: UpdateAppointmentRequest
    ): Result<Appointment> {
        return try {
            val response = apiService.updateAppointment(appointmentId, request)
            if (response.success && response.data != null) {
                appointmentDao.insertAppointment(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            val response = apiService.deleteAppointment(appointmentId)
            if (response.success) {
                // Remove from cache
                val appointment = appointmentDao.getAppointmentById(appointmentId)
                if (appointment != null) {
                    appointmentDao.deleteAppointment(appointment)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshAppointments(
        ownerId: String? = null,
        status: AppointmentStatus? = null,
        type: AppointmentType? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        try {
            val response = apiService.getAppointments(
                status = status?.name?.lowercase(),
                type = type?.name?.lowercase(),
                dateFrom = dateFrom,
                dateTo = dateTo,
                ownerId = ownerId
            )
            if (response.success) {
                appointmentDao.insertAppointments(response.data)
            }
        } catch (e: Exception) {
            // Handle error silently for background refresh
        }
    }
    
    suspend fun clearCache() {
        appointmentDao.deleteAllAppointments()
    }
} 