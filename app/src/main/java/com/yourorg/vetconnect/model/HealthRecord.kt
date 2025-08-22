package com.yourorg.vetconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yourorg.vetconnect.data.Converters

@Entity(tableName = "health_records")
@TypeConverters(Converters::class)
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val recordType: HealthRecordType,
    val title: String,
    val description: String,
    val date: Long,
    val veterinarianId: Long? = null,
    val nextDueDate: Long? = null,
    val isCompleted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HealthRecordType {
    VACCINATION,
    TREATMENT,
    CHECKUP,
    SURGERY,
    MEDICATION,
    ALLERGY,
    OTHER
}
