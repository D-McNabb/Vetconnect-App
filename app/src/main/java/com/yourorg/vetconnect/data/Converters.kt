package com.yourorg.vetconnect.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourorg.vetconnect.model.*
import java.time.DayOfWeek

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromUserType(value: UserType): String = value.name

    @TypeConverter
    fun toUserType(value: String): UserType = UserType.valueOf(value)

    @TypeConverter
    fun fromAppointmentStatus(value: AppointmentStatus): String = value.name

    @TypeConverter
    fun toAppointmentStatus(value: String): AppointmentStatus = AppointmentStatus.valueOf(value)

    @TypeConverter
    fun fromHealthRecordType(value: HealthRecordType): String = value.name

    @TypeConverter
    fun toHealthRecordType(value: String): HealthRecordType = HealthRecordType.valueOf(value)

    @TypeConverter
    fun fromAppointmentType(value: AppointmentType): String = value.name

    @TypeConverter
    fun toAppointmentType(value: String): AppointmentType = AppointmentType.valueOf(value)

    @TypeConverter
    fun fromUrgencyLevel(value: UrgencyLevel): String = value.name

    @TypeConverter
    fun toUrgencyLevel(value: String): UrgencyLevel = UrgencyLevel.valueOf(value)

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)

    @TypeConverter
    fun fromRecurringPattern(value: RecurringPattern?): String? = value?.name

    @TypeConverter
    fun toRecurringPattern(value: String?): RecurringPattern? = value?.let { RecurringPattern.valueOf(it) }

    @TypeConverter
    fun fromLongList(value: List<Long>): String = gson.toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> = gson.fromJson(value, object : TypeToken<List<Long>>() {}.type)

    @TypeConverter
    fun fromAppointmentTypeList(value: List<AppointmentType>): String = gson.toJson(value.map { it.name })

    @TypeConverter
    fun toAppointmentTypeList(value: String): List<AppointmentType> {
        val stringList: List<String> = gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
        return stringList.map { AppointmentType.valueOf(it) }
    }

    // Pet-related converters
    @TypeConverter
    fun fromPetSpecies(value: PetSpecies): String = value.name

    @TypeConverter
    fun toPetSpecies(value: String): PetSpecies = PetSpecies.valueOf(value)

    @TypeConverter
    fun fromPetGender(value: PetGender): String = value.name

    @TypeConverter
    fun toPetGender(value: String): PetGender = PetGender.valueOf(value)

    @TypeConverter
    fun fromReproductiveStatus(value: ReproductiveStatus): String = value.name

    @TypeConverter
    fun toReproductiveStatus(value: String): ReproductiveStatus = ReproductiveStatus.valueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun fromMedicationList(value: List<Medication>): String = gson.toJson(value)

    @TypeConverter
    fun toMedicationList(value: String): List<Medication> = gson.fromJson(value, object : TypeToken<List<Medication>>() {}.type)

    @TypeConverter
    fun fromMedicalRecordType(value: MedicalRecordType): String = value.name

    @TypeConverter
    fun toMedicalRecordType(value: String): MedicalRecordType = MedicalRecordType.valueOf(value)

    @TypeConverter
    fun fromSeverity(value: Severity): String = value.name

    @TypeConverter
    fun toSeverity(value: String): Severity = Severity.valueOf(value)

    @TypeConverter
    fun fromHealthStatus(value: HealthStatus): String = value.name

    @TypeConverter
    fun toHealthStatus(value: String): HealthStatus = HealthStatus.valueOf(value)
}
