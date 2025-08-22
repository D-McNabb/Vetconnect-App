package com.yourorg.vetconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yourorg.vetconnect.data.Converters
import java.time.DayOfWeek

@Entity(tableName = "vet_availability")
@TypeConverters(Converters::class)
data class VetAvailability(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val veterinarianId: Long,
    val clinicId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: String, // "09:00"
    val endTime: String,   // "17:00"
    val slotDuration: Int = 30, // 30 minutes
    val appointmentTypes: List<AppointmentType> = AppointmentType.values().toList(),
    val isActive: Boolean = true,
    val effectiveFrom: Long = System.currentTimeMillis(),
    val effectiveUntil: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_slots")
@TypeConverters(Converters::class)
data class BlockedSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val veterinarianId: Long,
    val startDateTime: Long,
    val endDateTime: Long,
    val reason: String,
    val isRecurring: Boolean = false,
    val recurringPattern: RecurringPattern? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecurringPattern {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

data class AvailableTimeSlot(
    val startTime: String,
    val endTime: String,
    val veterinarianId: Long,
    val veterinarianName: String,
    val appointmentType: AppointmentType,
    val isBooked: Boolean = false
)
