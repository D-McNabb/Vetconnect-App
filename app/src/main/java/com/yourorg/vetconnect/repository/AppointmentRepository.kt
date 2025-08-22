package com.yourorg.vetconnect.repository

import com.yourorg.vetconnect.data.AppDatabase
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
// import javax.inject.Inject
// import javax.inject.Singleton

// @Singleton
class AppointmentRepository(
    private val database: AppDatabase
) {
    private val appointmentDao = database.appointmentDao()
    private val vetAvailabilityDao = database.vetAvailabilityDao()
    private val userDao = database.userDao()
    private val petDao = database.petDao()

    // Appointment CRUD operations
    suspend fun createAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun cancelAppointment(appointmentId: Long, reason: String) {
        appointmentDao.cancelAppointment(appointmentId, AppointmentStatus.CANCELLED, reason)
    }

    suspend fun rescheduleAppointment(appointmentId: Long, newDate: Long, newStartTime: String, newEndTime: String) {
        val appointment = appointmentDao.getAppointmentById(appointmentId)
        appointment?.let {
            val updatedAppointment = it.copy(
                appointmentDate = newDate,
                startTime = newStartTime,
                endTime = newEndTime,
                status = AppointmentStatus.RESCHEDULED,
                updatedAt = System.currentTimeMillis()
            )
            appointmentDao.updateAppointment(updatedAppointment)
        }
    }

    suspend fun getAppointmentById(appointmentId: Long): Appointment? {
        return appointmentDao.getAppointmentById(appointmentId)
    }

    suspend fun getAppointmentWithDetails(appointmentId: Long): AppointmentWithDetails? {
        return appointmentDao.getAppointmentWithDetails(appointmentId)
    }

    // Query methods
    fun getAppointmentsByOwner(ownerId: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByOwner(ownerId)
    }

    fun getAppointmentsByVet(vetId: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsByVet(vetId)
    }

    fun getUpcomingAppointments(startDate: Long = System.currentTimeMillis()): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(startDate)
    }

    fun getAppointmentsInDateRange(startDate: Long, endDate: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsInDateRange(startDate, endDate)
    }

    // Availability methods
    suspend fun getAvailableTimeSlots(
        vetId: Long,
        date: LocalDate,
        appointmentType: AppointmentType,
        duration: Int = 30
    ): List<AvailableTimeSlot> {
        val dayOfWeek = date.dayOfWeek
        val dateInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayEndMillis = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Get veterinarian availability for this day
        val availability = vetAvailabilityDao.getActiveAvailabilityForDay(vetId, dayOfWeek, System.currentTimeMillis())
        if (availability.isEmpty()) return emptyList()

        // Get existing appointments for this date
        val existingAppointments = appointmentDao.getVetAppointmentsForDate(vetId, dateInMillis)

        // Get blocked slots for this date
        val blockedSlots = vetAvailabilityDao.getBlockedSlotsForDate(vetId, dateInMillis, dayEndMillis)

        val availableSlots = mutableListOf<AvailableTimeSlot>()

        availability.forEach { avail ->
            if (avail.appointmentTypes.contains(appointmentType)) {
                val slots = generateTimeSlots(
                    startTime = avail.startTime,
                    endTime = avail.endTime,
                    slotDuration = duration,
                    existingAppointments = existingAppointments,
                    blockedSlots = blockedSlots,
                    vetId = vetId
                )
                availableSlots.addAll(slots)
            }
        }

        return availableSlots.sortedBy { it.startTime }
    }

    private fun generateTimeSlots(
        startTime: String,
        endTime: String,
        slotDuration: Int,
        existingAppointments: List<Appointment>,
        blockedSlots: List<BlockedSlot>,
        vetId: Long
    ): List<AvailableTimeSlot> {
        val slots = mutableListOf<AvailableTimeSlot>()
        val start = LocalTime.parse(startTime)
        val end = LocalTime.parse(endTime)
        val veterinarianName = "Dr. Smith" // TODO: Get from user repository

        var currentTime = start
        while (currentTime.plusMinutes(slotDuration.toLong()) <= end) {
            val slotEndTime = currentTime.plusMinutes(slotDuration.toLong())
            
            val isBlocked = isTimeSlotBlocked(
                currentTime.toString(),
                slotEndTime.toString(),
                existingAppointments,
                blockedSlots
            )

            slots.add(
                AvailableTimeSlot(
                    startTime = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    endTime = slotEndTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    veterinarianId = vetId,
                    veterinarianName = veterinarianName,
                    appointmentType = AppointmentType.ROUTINE_CHECKUP, // Default
                    isBooked = isBlocked
                )
            )

            currentTime = currentTime.plusMinutes(slotDuration.toLong())
        }

        return slots
    }

    private fun isTimeSlotBlocked(
        startTime: String,
        endTime: String,
        existingAppointments: List<Appointment>,
        blockedSlots: List<BlockedSlot>
    ): Boolean {
        // Check existing appointments
        existingAppointments.forEach { appointment ->
            if (timeSlotsOverlap(startTime, endTime, appointment.startTime, appointment.endTime)) {
                return true
            }
        }

        // Check blocked slots
        blockedSlots.forEach { blockedSlot ->
            val blockedStart = LocalTime.ofInstant(
                java.time.Instant.ofEpochMilli(blockedSlot.startDateTime),
                ZoneId.systemDefault()
            )
            val blockedEnd = LocalTime.ofInstant(
                java.time.Instant.ofEpochMilli(blockedSlot.endDateTime),
                ZoneId.systemDefault()
            )
            
            if (timeSlotsOverlap(
                startTime, endTime,
                blockedStart.format(DateTimeFormatter.ofPattern("HH:mm")),
                blockedEnd.format(DateTimeFormatter.ofPattern("HH:mm"))
            )) {
                return true
            }
        }

        return false
    }

    private fun timeSlotsOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
        val startTime1 = LocalTime.parse(start1)
        val endTime1 = LocalTime.parse(end1)
        val startTime2 = LocalTime.parse(start2)
        val endTime2 = LocalTime.parse(end2)

        return startTime1 < endTime2 && startTime2 < endTime1
    }

    // Veterinarian availability management
    suspend fun addVetAvailability(availability: VetAvailability): Long {
        return vetAvailabilityDao.insertAvailability(availability)
    }

    suspend fun updateVetAvailability(availability: VetAvailability) {
        vetAvailabilityDao.updateAvailability(availability)
    }

    suspend fun removeVetAvailability(availabilityId: Long) {
        vetAvailabilityDao.deactivateAvailability(availabilityId)
    }

    fun getVetAvailability(vetId: Long): Flow<List<VetAvailability>> {
        return vetAvailabilityDao.getVetAvailability(vetId)
    }

    // Blocked slots management
    suspend fun addBlockedSlot(blockedSlot: BlockedSlot): Long {
        return vetAvailabilityDao.insertBlockedSlot(blockedSlot)
    }

    suspend fun removeBlockedSlot(blockedSlotId: Long) {
        vetAvailabilityDao.deleteBlockedSlotById(blockedSlotId)
    }

    // Statistics and analytics
    suspend fun getAppointmentStats(startDate: Long, endDate: Long): AppointmentStats {
        val totalAppointments = appointmentDao.getAppointmentCountByStatus(AppointmentStatus.COMPLETED, startDate, endDate)
        val cancelledAppointments = appointmentDao.getAppointmentCountByStatus(AppointmentStatus.CANCELLED, startDate, endDate)
        val noShowAppointments = appointmentDao.getAppointmentCountByStatus(AppointmentStatus.NO_SHOW, startDate, endDate)
        
        return AppointmentStats(
            totalAppointments = totalAppointments,
            cancelledAppointments = cancelledAppointments,
            noShowAppointments = noShowAppointments,
            completionRate = if (totalAppointments > 0) (totalAppointments.toDouble() / (totalAppointments + cancelledAppointments + noShowAppointments)) * 100 else 0.0
        )
    }

    suspend fun getAppointmentCountsByType(startDate: Long, endDate: Long): Map<AppointmentType, Int> {
        val counts = appointmentDao.getAppointmentCountsByType(startDate, endDate)
        return counts.associate { 
            AppointmentType.valueOf(it.appointmentType) to it.count 
        }
    }
}

data class AppointmentStats(
    val totalAppointments: Int,
    val cancelledAppointments: Int,
    val noShowAppointments: Int,
    val completionRate: Double
)
