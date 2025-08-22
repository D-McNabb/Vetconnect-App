package com.yourorg.vetconnect.data

import androidx.room.*
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface VetAvailabilityDao {
    // Availability management
    @Query("SELECT * FROM vet_availability WHERE veterinarianId = :vetId AND isActive = 1")
    fun getVetAvailability(vetId: Long): Flow<List<VetAvailability>>

    @Query("SELECT * FROM vet_availability WHERE veterinarianId = :vetId AND dayOfWeek = :dayOfWeek AND isActive = 1")
    suspend fun getVetAvailabilityForDay(vetId: Long, dayOfWeek: DayOfWeek): List<VetAvailability>

    @Query("SELECT * FROM vet_availability WHERE clinicId = :clinicId AND dayOfWeek = :dayOfWeek AND isActive = 1")
    suspend fun getClinicAvailabilityForDay(clinicId: Long, dayOfWeek: DayOfWeek): List<VetAvailability>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailability(availability: VetAvailability): Long

    @Update
    suspend fun updateAvailability(availability: VetAvailability)

    @Delete
    suspend fun deleteAvailability(availability: VetAvailability)

    @Query("UPDATE vet_availability SET isActive = 0 WHERE id = :availabilityId")
    suspend fun deactivateAvailability(availabilityId: Long)

    // Blocked slots management
    @Query("""
        SELECT * FROM blocked_slots 
        WHERE veterinarianId = :vetId 
        AND startDateTime <= :endDateTime 
        AND endDateTime >= :startDateTime
    """)
    suspend fun getBlockedSlotsInRange(vetId: Long, startDateTime: Long, endDateTime: Long): List<BlockedSlot>

    @Query("""
        SELECT * FROM blocked_slots 
        WHERE veterinarianId = :vetId 
        AND startDateTime >= :dateStart 
        AND endDateTime <= :dateEnd
    """)
    suspend fun getBlockedSlotsForDate(vetId: Long, dateStart: Long, dateEnd: Long): List<BlockedSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedSlot(blockedSlot: BlockedSlot): Long

    @Update
    suspend fun updateBlockedSlot(blockedSlot: BlockedSlot)

    @Delete
    suspend fun deleteBlockedSlot(blockedSlot: BlockedSlot)

    @Query("DELETE FROM blocked_slots WHERE id = :blockedSlotId")
    suspend fun deleteBlockedSlotById(blockedSlotId: Long)

    // Get available time slots for a specific veterinarian and date
    @Query("""
        SELECT va.* FROM vet_availability va
        WHERE va.veterinarianId = :vetId 
        AND va.dayOfWeek = :dayOfWeek 
        AND va.isActive = 1
        AND (va.effectiveUntil IS NULL OR va.effectiveUntil > :currentTime)
        AND va.effectiveFrom <= :currentTime
    """)
    suspend fun getActiveAvailabilityForDay(vetId: Long, dayOfWeek: DayOfWeek, currentTime: Long): List<VetAvailability>
}
