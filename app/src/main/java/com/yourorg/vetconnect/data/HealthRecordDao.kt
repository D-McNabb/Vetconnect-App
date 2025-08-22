package com.yourorg.vetconnect.data

import androidx.room.*
import com.yourorg.vetconnect.model.HealthRecord
import com.yourorg.vetconnect.model.HealthRecordType
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE petId = :petId ORDER BY date DESC")
    fun getHealthRecordsByPet(petId: Long): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND recordType = :recordType ORDER BY date DESC")
    fun getHealthRecordsByPetAndType(petId: Long, recordType: HealthRecordType): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE nextDueDate >= :currentDate ORDER BY nextDueDate ASC")
    fun getUpcomingHealthRecords(currentDate: Long): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE id = :recordId")
    suspend fun getHealthRecordById(recordId: Long): HealthRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(healthRecord: HealthRecord): Long

    @Update
    suspend fun updateHealthRecord(healthRecord: HealthRecord)

    @Delete
    suspend fun deleteHealthRecord(healthRecord: HealthRecord)

    @Query("SELECT COUNT(*) FROM health_records WHERE petId = :petId AND recordType = 'VACCINATION'")
    suspend fun getVaccinationCountForPet(petId: Long): Int
}
