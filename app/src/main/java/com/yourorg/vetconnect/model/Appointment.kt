package com.yourorg.vetconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yourorg.vetconnect.data.Converters

@Entity(tableName = "appointments")
@TypeConverters(Converters::class)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val ownerId: Long,
    val veterinarianId: Long,
    val clinicId: Long = 1, // Default clinic
    val appointmentType: AppointmentType,
    val appointmentDate: Long, // Date in milliseconds
    val startTime: String, // "09:00"
    val endTime: String, // "09:30"
    val duration: Int = 30, // Duration in minutes
    val reason: String,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.MEDIUM,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String? = null,
    val preparationInstructions: String? = null,
    val estimatedCost: Double? = null,
    val actualCost: Double? = null,
    val cancellationReason: String? = null,
    val remindersSent: List<Long> = emptyList(), // Timestamps of sent reminders
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED
}

enum class AppointmentType {
    ROUTINE_CHECKUP,
    VACCINATION,
    SURGERY,
    EMERGENCY,
    CONSULTATION,
    FOLLOW_UP,
    DENTAL,
    GROOMING,
    WELLNESS_EXAM,
    DIAGNOSTIC
}

enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    EMERGENCY
}

// Data classes for appointment scheduling
data class AppointmentSlot(
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean,
    val veterinarianId: Long? = null,
    val appointmentId: Long? = null
)

data class AppointmentWithDetails(
    // Appointment fields
    val id: Long,
    val petId: Long,
    val ownerId: Long,
    val veterinarianId: Long,
    val clinicId: Long,
    val appointmentType: AppointmentType,
    val appointmentDate: Long,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val reason: String,
    val urgencyLevel: UrgencyLevel,
    val status: AppointmentStatus,
    val notes: String?,
    val preparationInstructions: String?,
    val estimatedCost: Double?,
    val actualCost: Double?,
    val cancellationReason: String?,
    val remindersSent: List<Long>,
    val createdAt: Long,
    val updatedAt: Long,
    // Additional fields from joins
    val petName: String,
    val ownerName: String,
    val veterinarianName: String
)

data class AppointmentTypeCount(
    val appointmentType: String,
    val count: Int
)
