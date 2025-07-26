package com.example.vetconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey
    @SerializedName("_id")
    val id: String,
    @SerializedName("petId")
    val petId: String,
    @SerializedName("ownerId")
    val ownerId: String,
    @SerializedName("veterinarianId")
    val veterinarianId: String? = null,
    @SerializedName("date")
    val date: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("type")
    val type: AppointmentType,
    @SerializedName("status")
    val status: AppointmentStatus,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("pet")
    val pet: Pet? = null,
    @SerializedName("owner")
    val owner: User? = null,
    @SerializedName("veterinarian")
    val veterinarian: User? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    val dateTime: String
        get() = "$date $time"
}

enum class AppointmentType {
    @SerializedName("checkup")
    CHECKUP,
    @SerializedName("vaccination")
    VACCINATION,
    @SerializedName("surgery")
    SURGERY,
    @SerializedName("emergency")
    EMERGENCY,
    @SerializedName("follow_up")
    FOLLOW_UP,
    @SerializedName("consultation")
    CONSULTATION
}

enum class AppointmentStatus {
    @SerializedName("scheduled")
    SCHEDULED,
    @SerializedName("confirmed")
    CONFIRMED,
    @SerializedName("in_progress")
    IN_PROGRESS,
    @SerializedName("completed")
    COMPLETED,
    @SerializedName("cancelled")
    CANCELLED,
    @SerializedName("no_show")
    NO_SHOW
}

data class CreateAppointmentRequest(
    val petId: String,
    val date: String,
    val time: String,
    val type: AppointmentType,
    val reason: String,
    val notes: String? = null
)

data class UpdateAppointmentRequest(
    val date: String? = null,
    val time: String? = null,
    val type: AppointmentType? = null,
    val status: AppointmentStatus? = null,
    val reason: String? = null,
    val notes: String? = null,
    val veterinarianId: String? = null
)

data class AppointmentFilter(
    val status: AppointmentStatus? = null,
    val type: AppointmentType? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val petId: String? = null,
    val ownerId: String? = null
) 