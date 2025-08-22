package com.yourorg.vetconnect.data

import androidx.room.*
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    // Basic appointment queries
    @Query("SELECT * FROM appointments WHERE ownerId = :ownerId ORDER BY appointmentDate DESC, startTime ASC")
    fun getAppointmentsByOwner(ownerId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE veterinarianId = :vetId ORDER BY appointmentDate DESC, startTime ASC")
    fun getAppointmentsByVet(vetId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE petId = :petId ORDER BY appointmentDate DESC, startTime ASC")
    fun getAppointmentsByPet(petId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE appointmentDate >= :startDate ORDER BY appointmentDate ASC, startTime ASC")
    fun getUpcomingAppointments(startDate: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Long): Appointment?

    // Calendar and scheduling queries
    @Query("""
        SELECT * FROM appointments 
        WHERE appointmentDate >= :startDate AND appointmentDate <= :endDate 
        ORDER BY appointmentDate ASC, startTime ASC
    """)
    fun getAppointmentsInDateRange(startDate: Long, endDate: Long): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE veterinarianId = :vetId AND appointmentDate = :date 
        AND status NOT IN ('CANCELLED', 'NO_SHOW')
        ORDER BY startTime ASC
    """)
    suspend fun getVetAppointmentsForDate(vetId: Long, date: Long): List<Appointment>

    @Query("""
        SELECT * FROM appointments 
        WHERE veterinarianId = :vetId AND appointmentDate = :date 
        AND startTime = :startTime AND status NOT IN ('CANCELLED', 'NO_SHOW')
    """)
    suspend fun getAppointmentAtTime(vetId: Long, date: Long, startTime: String): Appointment?

    @Query("""
        SELECT COUNT(*) FROM appointments 
        WHERE veterinarianId = :vetId AND appointmentDate = :date 
        AND status NOT IN ('CANCELLED', 'NO_SHOW')
    """)
    suspend fun getActiveAppointmentCountForVetOnDate(vetId: Long, date: Long): Int

    // Appointment with details query
    @Query("""
        SELECT a.id, a.petId, a.ownerId, a.veterinarianId, a.clinicId, 
               a.appointmentType, a.appointmentDate, a.startTime, a.endTime, 
               a.duration, a.reason, a.urgencyLevel, a.status, a.notes, 
               a.preparationInstructions, a.estimatedCost, a.actualCost, 
               a.cancellationReason, a.remindersSent, a.createdAt, a.updatedAt,
               COALESCE(p.name, 'Unknown Pet') as petName,
               COALESCE(u_owner.firstName || ' ' || u_owner.lastName, 'Unknown Owner') as ownerName,
               COALESCE('Dr. ' || u_vet.firstName || ' ' || u_vet.lastName, 'Unknown Veterinarian') as veterinarianName
        FROM appointments a
        LEFT JOIN pets p ON a.petId = p.id
        LEFT JOIN users u_owner ON a.ownerId = u_owner.id
        LEFT JOIN users u_vet ON a.veterinarianId = u_vet.id
        WHERE a.id = :appointmentId
    """)
    suspend fun getAppointmentWithDetails(appointmentId: Long): AppointmentWithDetails?

    // CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("UPDATE appointments SET status = :status, updatedAt = :updatedAt WHERE id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: Long, status: AppointmentStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE appointments SET status = :status, cancellationReason = :reason, updatedAt = :updatedAt WHERE id = :appointmentId")
    suspend fun cancelAppointment(appointmentId: Long, status: AppointmentStatus, reason: String, updatedAt: Long = System.currentTimeMillis())

    // Statistics and analytics
    @Query("""
        SELECT COUNT(*) FROM appointments 
        WHERE status = :status AND appointmentDate >= :startDate AND appointmentDate <= :endDate
    """)
    suspend fun getAppointmentCountByStatus(status: AppointmentStatus, startDate: Long, endDate: Long): Int

    @Query("""
        SELECT appointmentType, COUNT(*) as count
        FROM appointments 
        WHERE appointmentDate >= :startDate AND appointmentDate <= :endDate
        GROUP BY appointmentType
    """)
    suspend fun getAppointmentCountsByType(startDate: Long, endDate: Long): List<AppointmentTypeCount>
}
