package com.example.vetconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("species")
    val species: String,
    @SerializedName("breed")
    val breed: String,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String,
    @SerializedName("gender")
    val gender: PetGender,
    @SerializedName("color")
    val color: String,
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("microchipId")
    val microchipId: String? = null,
    @SerializedName("ownerId")
    val ownerId: String,
    @SerializedName("medicalHistory")
    val medicalHistory: List<MedicalRecord> = emptyList(),
    @SerializedName("vaccinations")
    val vaccinations: List<Vaccination> = emptyList(),
    @SerializedName("allergies")
    val allergies: List<String> = emptyList(),
    @SerializedName("medications")
    val medications: List<Medication> = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    val age: String
        get() {
            // Calculate age from dateOfBirth
            return "Adult" // Simplified for now
        }
}

enum class PetGender {
    @SerializedName("male")
    MALE,
    @SerializedName("female")
    FEMALE
}

data class MedicalRecord(
    @SerializedName("_id")
    val id: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("diagnosis")
    val diagnosis: String,
    @SerializedName("treatment")
    val treatment: String,
    @SerializedName("veterinarian")
    val veterinarian: String,
    @SerializedName("notes")
    val notes: String? = null
)

data class Vaccination(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("nextDueDate")
    val nextDueDate: String? = null,
    @SerializedName("veterinarian")
    val veterinarian: String
)

data class Medication(
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("dosage")
    val dosage: String,
    @SerializedName("frequency")
    val frequency: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("notes")
    val notes: String? = null
)

data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String,
    val dateOfBirth: String,
    val gender: PetGender,
    val color: String,
    val weight: Double,
    val microchipId: String? = null
)

data class UpdatePetRequest(
    val name: String? = null,
    val species: String? = null,
    val breed: String? = null,
    val dateOfBirth: String? = null,
    val gender: PetGender? = null,
    val color: String? = null,
    val weight: Double? = null,
    val microchipId: String? = null
) 