package com.yourorg.vetconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yourorg.vetconnect.data.Converters

@Entity(tableName = "pets")
@TypeConverters(Converters::class)
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long,
    
    // Basic Information
    val name: String,
    val species: PetSpecies,
    val breed: String,
    val dateOfBirth: Long,
    val gender: PetGender,
    val reproductiveStatus: ReproductiveStatus,
    
    // Physical Characteristics
    val weight: Double, // in kg
    val height: Double? = null, // in cm
    val length: Double? = null, // in cm
    val color: String,
    val markings: String? = null,
    val distinguishingFeatures: String? = null,
    
    // Identification
    val microchipNumber: String? = null,
    val registrationNumber: String? = null,
    val tattoNumber: String? = null,
    
    // Medical Information
    val bloodType: String? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val behavioralNotes: String? = null,
    
    // Emergency Information
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val preferredVeterinarianId: Long? = null,
    
    // Insurance and Financial
    val insuranceProvider: String? = null,
    val insurancePolicyNumber: String? = null,
    val insuranceExpiryDate: Long? = null,
    
    // Profile and Documentation
    val profileImageUrl: String? = null,
    val additionalNotes: String? = null,
    
    // System Fields
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PetSpecies {
    DOG,
    CAT,
    BIRD,
    RABBIT,
    HAMSTER,
    GUINEA_PIG,
    FERRET,
    REPTILE,
    FISH,
    HORSE,
    COW,
    SHEEP,
    GOAT,
    PIG,
    OTHER
}

enum class PetGender {
    MALE,
    FEMALE,
    UNKNOWN
}

enum class ReproductiveStatus {
    INTACT,
    NEUTERED,
    SPAYED,
    UNKNOWN
}

// Medical History and Records
@Entity(tableName = "medical_records")
@TypeConverters(Converters::class)
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val veterinarianId: Long,
    val appointmentId: Long? = null,
    val recordType: MedicalRecordType,
    val title: String,
    val description: String,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val medications: List<Medication> = emptyList(),
    val attachments: List<String> = emptyList(), // File URLs/paths
    val severity: Severity = Severity.MEDIUM,
    val isResolved: Boolean = false,
    val followUpRequired: Boolean = false,
    val followUpDate: Long? = null,
    val recordDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vaccinations")
@TypeConverters(Converters::class)
data class Vaccination(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val veterinarianId: Long,
    val vaccineName: String,
    val manufacturer: String,
    val batchNumber: String,
    val administeredDate: Long,
    val expirationDate: Long,
    val nextDueDate: Long? = null,
    val injectionSite: String, // e.g., "Left shoulder", "Right thigh"
    val reactions: String? = null,
    val certificationNumber: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val weight: Double, // in kg
    val bodyConditionScore: Int? = null, // 1-9 scale
    val muscleConditionScore: Int? = null, // 1-3 scale
    val notes: String? = null,
    val recordedBy: Long, // veterinarian or staff ID
    val recordDate: Long = System.currentTimeMillis()
)

data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String? = null,
    val startDate: Long,
    val endDate: Long? = null
)

enum class MedicalRecordType {
    EXAMINATION,
    DIAGNOSIS,
    TREATMENT,
    SURGERY,
    VACCINATION,
    LAB_RESULT,
    IMAGING,
    EMERGENCY,
    FOLLOW_UP,
    PRESCRIPTION,
    OTHER
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// Data classes for complex forms and views
data class PetProfile(
    val pet: Pet,
    val ownerName: String,
    val recentMedicalRecords: List<MedicalRecord>,
    val upcomingVaccinations: List<Vaccination>,
    val currentWeight: WeightRecord?
)

data class PetHealthSummary(
    val petId: Long,
    val petName: String,
    val lastCheckupDate: Long?,
    val nextVaccinationDue: Long?,
    val activeConditions: Int,
    val healthStatus: HealthStatus
)

enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL,
    UNKNOWN
}
