package com.example.vetconnect.data.local

import androidx.room.TypeConverter
import com.example.vetconnect.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMedicalRecordList(value: List<MedicalRecord>?): String? = gson.toJson(value)

    @TypeConverter
    fun toMedicalRecordList(value: String?): List<MedicalRecord>? =
        gson.fromJson(value, object : TypeToken<List<MedicalRecord>>() {}.type)

    @TypeConverter
    fun fromVaccinationList(value: List<Vaccination>?): String? = gson.toJson(value)

    @TypeConverter
    fun toVaccinationList(value: String?): List<Vaccination>? =
        gson.fromJson(value, object : TypeToken<List<Vaccination>>() {}.type)

    @TypeConverter
    fun fromMedicationList(value: List<Medication>?): String? = gson.toJson(value)

    @TypeConverter
    fun toMedicationList(value: String?): List<Medication>? =
        gson.fromJson(value, object : TypeToken<List<Medication>>() {}.type)

    @TypeConverter
    fun fromPet(pet: Pet?): String? = gson.toJson(pet)

    @TypeConverter
    fun toPet(value: String?): Pet? =
        gson.fromJson(value, object : TypeToken<Pet>() {}.type)

    @TypeConverter
    fun fromUser(user: User?): String? = gson.toJson(user)

    @TypeConverter
    fun toUser(value: String?): User? =
        gson.fromJson(value, object : TypeToken<User>() {}.type)

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = Gson().toJson(list)

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return data?.let { Gson().fromJson(it, listType) }
    }
} 