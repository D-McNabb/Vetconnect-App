package com.yourorg.vetconnect.data

import androidx.room.*
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalRecordDao {
    // Basic CRUD operations
    @Query("SELECT * FROM medical_records WHERE petId = :petId ORDER BY recordDate DESC")
    fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecord>>

    @Query("SELECT * FROM medical_records WHERE id = :recordId")
    suspend fun getMedicalRecordById(recordId: Long): MedicalRecord?

    @Query("SELECT * FROM medical_records WHERE veterinarianId = :vetId ORDER BY recordDate DESC")
    fun getMedicalRecordsByVet(vetId: Long): Flow<List<MedicalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecord(record: MedicalRecord): Long

    @Update
    suspend fun updateMedicalRecord(record: MedicalRecord)

    @Delete
    suspend fun deleteMedicalRecord(record: MedicalRecord)

    // Filtered queries
    @Query("""
        SELECT * FROM medical_records 
        WHERE petId = :petId AND recordType = :recordType 
        ORDER BY recordDate DESC
    """)
    fun getMedicalRecordsByType(petId: Long, recordType: MedicalRecordType): Flow<List<MedicalRecord>>

    @Query("""
        SELECT * FROM medical_records 
        WHERE petId = :petId AND isResolved = 0 
        ORDER BY severity DESC, recordDate DESC
    """)
    fun getActiveMedicalRecords(petId: Long): Flow<List<MedicalRecord>>

    @Query("""
        SELECT * FROM medical_records 
        WHERE petId = :petId AND followUpRequired = 1 AND followUpDate <= :currentDate
        ORDER BY followUpDate ASC
    """)
    suspend fun getOverdueFollowUps(petId: Long, currentDate: Long): List<MedicalRecord>

    @Query("""
        SELECT * FROM medical_records 
        WHERE recordDate >= :startDate AND recordDate <= :endDate
        ORDER BY recordDate DESC
    """)
    suspend fun getMedicalRecordsInDateRange(startDate: Long, endDate: Long): List<MedicalRecord>

    // Search functionality
    @Query("""
        SELECT * FROM medical_records 
        WHERE petId = :petId 
        AND (title LIKE '%' || :searchQuery || '%' 
             OR description LIKE '%' || :searchQuery || '%'
             OR diagnosis LIKE '%' || :searchQuery || '%')
        ORDER BY recordDate DESC
    """)
    suspend fun searchMedicalRecords(petId: Long, searchQuery: String): List<MedicalRecord>

    // Statistics and reports
    @Query("""
        SELECT COUNT(*) FROM medical_records 
        WHERE petId = :petId AND recordType = :recordType
    """)
    suspend fun getRecordCountByType(petId: Long, recordType: MedicalRecordType): Int

    @Query("""
        SELECT * FROM medical_records 
        WHERE petId = :petId 
        ORDER BY recordDate DESC 
        LIMIT :limit
    """)
    suspend fun getRecentMedicalRecords(petId: Long, limit: Int = 5): List<MedicalRecord>
}

@Dao
interface VaccinationDao {
    // Basic operations
    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY administeredDate DESC")
    fun getVaccinationsByPet(petId: Long): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE id = :vaccinationId")
    suspend fun getVaccinationById(vaccinationId: Long): Vaccination?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: Vaccination): Long

    @Update
    suspend fun updateVaccination(vaccination: Vaccination)

    @Delete
    suspend fun deleteVaccination(vaccination: Vaccination)

    // Vaccination schedule management
    @Query("""
        SELECT * FROM vaccinations 
        WHERE petId = :petId AND nextDueDate IS NOT NULL 
        ORDER BY nextDueDate ASC
    """)
    fun getUpcomingVaccinations(petId: Long): Flow<List<Vaccination>>

    @Query("""
        SELECT * FROM vaccinations 
        WHERE nextDueDate <= :dueDate AND nextDueDate IS NOT NULL
        ORDER BY nextDueDate ASC
    """)
    suspend fun getOverdueVaccinations(dueDate: Long): List<Vaccination>

    @Query("""
        SELECT * FROM vaccinations 
        WHERE petId = :petId AND vaccineName = :vaccineName 
        ORDER BY administeredDate DESC 
        LIMIT 1
    """)
    suspend fun getLastVaccinationByName(petId: Long, vaccineName: String): Vaccination?

    // Vaccination history and certificates
    @Query("""
        SELECT * FROM vaccinations 
        WHERE petId = :petId AND certificationNumber IS NOT NULL
        ORDER BY administeredDate DESC
    """)
    suspend fun getCertifiedVaccinations(petId: Long): List<Vaccination>

    @Query("""
        SELECT COUNT(*) FROM vaccinations 
        WHERE petId = :petId AND administeredDate >= :startDate
    """)
    suspend fun getVaccinationCountSince(petId: Long, startDate: Long): Int
}

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY recordDate DESC")
    fun getWeightRecordsByPet(petId: Long): Flow<List<WeightRecord>>

    @Query("""
        SELECT * FROM weight_records 
        WHERE petId = :petId 
        ORDER BY recordDate DESC 
        LIMIT 1
    """)
    suspend fun getLatestWeightRecord(petId: Long): WeightRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(record: WeightRecord): Long

    @Update
    suspend fun updateWeightRecord(record: WeightRecord)

    @Delete
    suspend fun deleteWeightRecord(record: WeightRecord)

    @Query("""
        SELECT * FROM weight_records 
        WHERE petId = :petId AND recordDate >= :startDate AND recordDate <= :endDate
        ORDER BY recordDate ASC
    """)
    suspend fun getWeightRecordsInRange(petId: Long, startDate: Long, endDate: Long): List<WeightRecord>

    @Query("""
        SELECT AVG(weight) FROM weight_records 
        WHERE petId = :petId AND recordDate >= :startDate
    """)
    suspend fun getAverageWeightSince(petId: Long, startDate: Long): Double?
}
