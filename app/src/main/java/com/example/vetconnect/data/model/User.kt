package com.example.vetconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("_id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("role")
    val role: UserRole,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    val fullName: String
        get() = "$firstName $lastName"
}

enum class UserRole {
    @SerializedName("pet_owner")
    PET_OWNER,
    @SerializedName("admin")
    ADMIN,
    @SerializedName("veterinarian")
    VETERINARIAN
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val address: String? = null
)

data class AuthResponse(
    val user: User,
    val token: String,
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val password: String
) 