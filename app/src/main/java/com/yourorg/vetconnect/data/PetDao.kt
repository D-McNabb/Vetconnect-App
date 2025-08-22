package com.yourorg.vetconnect.data

import androidx.room.*
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    // Basic pet operations
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId AND isActive = 1 ORDER BY name ASC")
    fun getPetsByOwner(ownerId: Long): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE id = :petId AND isActive = 1")
    suspend fun getPetById(petId: Long): Pet?

    @Query("SELECT * FROM pets WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActivePets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE preferredVeterinarianId = :vetId AND isActive = 1")
    fun getPetsByVeterinarian(vetId: Long): Flow<List<Pet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long

    @Update
    suspend fun updatePet(pet: Pet)

    @Query("UPDATE pets SET isActive = 0, updatedAt = :updatedAt WHERE id = :petId")
    suspend fun deletePet(petId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM pets WHERE ownerId = :ownerId AND isActive = 1")
    suspend fun getPetCountForOwner(ownerId: Long): Int

    // Search and filter operations
    @Query("""
        SELECT * FROM pets 
        WHERE isActive = 1 
        AND (name LIKE '%' || :searchQuery || '%' 
             OR breed LIKE '%' || :searchQuery || '%'
             OR microchipNumber LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
    """)
    suspend fun searchPets(searchQuery: String): List<Pet>

    @Query("SELECT * FROM pets WHERE species = :species AND isActive = 1 ORDER BY name ASC")
    suspend fun getPetsBySpecies(species: PetSpecies): List<Pet>

    @Query("SELECT * FROM pets WHERE breed LIKE '%' || :breed || '%' AND isActive = 1 ORDER BY name ASC")
    suspend fun getPetsByBreed(breed: String): List<Pet>

    // Medical-related queries
    @Query("""
        SELECT p.* FROM pets p
        INNER JOIN medical_records mr ON p.id = mr.petId
        WHERE mr.recordType = :recordType 
        AND mr.isResolved = 0
        AND p.isActive = 1
        ORDER BY p.name ASC
    """)
    suspend fun getPetsWithActiveCondition(recordType: MedicalRecordType): List<Pet>

    @Query("""
        SELECT p.* FROM pets p
        INNER JOIN vaccinations v ON p.id = v.petId
        WHERE v.nextDueDate <= :dueDate
        AND p.isActive = 1
        ORDER BY v.nextDueDate ASC
    """)
    suspend fun getPetsWithUpcomingVaccinations(dueDate: Long): List<Pet>

    // Pet profile with related data
    @Transaction
    @Query("""
        SELECT p.*, 
               u.firstName || ' ' || u.lastName as ownerName
        FROM pets p
        LEFT JOIN users u ON p.ownerId = u.id
        WHERE p.id = :petId AND p.isActive = 1
    """)
    suspend fun getPetProfile(petId: Long): PetWithOwner?

    // Health statistics
    @Query("""
        SELECT COUNT(*) FROM pets p
        INNER JOIN medical_records mr ON p.id = mr.petId
        WHERE p.ownerId = :ownerId 
        AND mr.severity = :severity
        AND mr.isResolved = 0
        AND p.isActive = 1
    """)
    suspend fun getActiveConditionsCount(ownerId: Long, severity: Severity): Int
}

data class PetWithOwner(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val species: PetSpecies,
    val breed: String,
    val dateOfBirth: Long,
    val gender: PetGender,
    val reproductiveStatus: ReproductiveStatus,
    val weight: Double,
    val height: Double?,
    val length: Double?,
    val color: String,
    val markings: String?,
    val distinguishingFeatures: String?,
    val microchipNumber: String?,
    val registrationNumber: String?,
    val tattoNumber: String?,
    val bloodType: String?,
    val allergies: List<String>,
    val chronicConditions: List<String>,
    val currentMedications: List<String>,
    val dietaryRestrictions: List<String>,
    val behavioralNotes: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val preferredVeterinarianId: Long?,
    val insuranceProvider: String?,
    val insurancePolicyNumber: String?,
    val insuranceExpiryDate: Long?,
    val profileImageUrl: String?,
    val additionalNotes: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val ownerName: String
)
