package com.yourorg.vetconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String, // In production, this should be hashed
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val userType: UserType,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserType {
    PET_OWNER,
    VETERINARIAN
}
