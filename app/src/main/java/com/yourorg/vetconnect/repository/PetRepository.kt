package com.yourorg.vetconnect.repository

import com.yourorg.vetconnect.data.AppDatabase
import com.yourorg.vetconnect.data.PetWithOwner
import com.yourorg.vetconnect.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class PetRepository(
    private val database: AppDatabase
) {
    private val petDao = database.petDao()
    private val medicalRecordDao = database.medicalRecordDao()
    private val vaccinationDao = database.vaccinationDao()
    private val weightRecordDao = database.weightRecordDao()

    // Pet CRUD operations
    suspend fun createPet(pet: Pet): Long {
        return petDao.insertPet(pet)
    }

    suspend fun updatePet(pet: Pet) {
        petDao.updatePet(pet.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePet(petId: Long) {
        petDao.deletePet(petId)
    }

    suspend fun getPetById(petId: Long): Pet? {
        return petDao.getPetById(petId)
    }

    fun getPetsByOwner(ownerId: Long): Flow<List<Pet>> {
        return petDao.getPetsByOwner(ownerId)
    }

    fun getAllActivePets(): Flow<List<Pet>> {
        return petDao.getAllActivePets()
    }

    // Advanced pet queries
    suspend fun searchPets(query: String): List<Pet> {
        return petDao.searchPets(query)
    }

    suspend fun getPetsBySpecies(species: PetSpecies): List<Pet> {
        return petDao.getPetsBySpecies(species)
    }

    suspend fun getPetsByBreed(breed: String): List<Pet> {
        return petDao.getPetsByBreed(breed)
    }

    // Pet profile with comprehensive information
    suspend fun getPetProfile(petId: Long): PetProfile? {
        val petWithOwner = petDao.getPetProfile(petId) ?: return null
        val recentMedicalRecords = medicalRecordDao.getRecentMedicalRecords(petId, 10)
        val upcomingVaccinations = vaccinationDao.getUpcomingVaccinations(petId)
        val currentWeight = weightRecordDao.getLatestWeightRecord(petId)
        
        return PetProfile(
            pet = petWithOwner.toPet(),
            ownerName = petWithOwner.ownerName,
            recentMedicalRecords = recentMedicalRecords,
            upcomingVaccinations = emptyList(), // Will be populated from flow
            currentWeight = currentWeight
        )
    }

    // Medical Records Management
    suspend fun addMedicalRecord(record: MedicalRecord): Long {
        return medicalRecordDao.insertMedicalRecord(record)
    }

    suspend fun updateMedicalRecord(record: MedicalRecord) {
        medicalRecordDao.updateMedicalRecord(record)
    }

    fun getMedicalRecordsByPet(petId: Long): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getMedicalRecordsByPet(petId)
    }

    fun getActiveMedicalRecords(petId: Long): Flow<List<MedicalRecord>> {
        return medicalRecordDao.getActiveMedicalRecords(petId)
    }

    suspend fun searchMedicalRecords(petId: Long, query: String): List<MedicalRecord> {
        return medicalRecordDao.searchMedicalRecords(petId, query)
    }

    // Vaccination Management
    suspend fun addVaccination(vaccination: Vaccination): Long {
        return vaccinationDao.insertVaccination(vaccination)
    }

    suspend fun updateVaccination(vaccination: Vaccination) {
        vaccinationDao.updateVaccination(vaccination)
    }

    fun getVaccinationsByPet(petId: Long): Flow<List<Vaccination>> {
        return vaccinationDao.getVaccinationsByPet(petId)
    }

    fun getUpcomingVaccinations(petId: Long): Flow<List<Vaccination>> {
        return vaccinationDao.getUpcomingVaccinations(petId)
    }

    suspend fun getOverdueVaccinations(currentDate: Long = System.currentTimeMillis()): List<Vaccination> {
        return vaccinationDao.getOverdueVaccinations(currentDate)
    }

    // Weight Management
    suspend fun addWeightRecord(record: WeightRecord): Long {
        return weightRecordDao.insertWeightRecord(record)
    }

    fun getWeightRecordsByPet(petId: Long): Flow<List<WeightRecord>> {
        return weightRecordDao.getWeightRecordsByPet(petId)
    }

    suspend fun getLatestWeight(petId: Long): WeightRecord? {
        return weightRecordDao.getLatestWeightRecord(petId)
    }

    suspend fun getWeightHistory(petId: Long, days: Int = 30): List<WeightRecord> {
        val startDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val endDate = System.currentTimeMillis()
        return weightRecordDao.getWeightRecordsInRange(petId, startDate, endDate)
    }

    // Health Analytics and Summary
    suspend fun getPetHealthSummary(petId: Long): PetHealthSummary? {
        val pet = getPetById(petId) ?: return null
        val lastCheckup = getLastCheckupDate(petId)
        val nextVaccination = getNextVaccinationDate(petId)
        val activeConditions = getActiveConditionsCount(petId)
        val healthStatus = calculateHealthStatus(petId)

        return PetHealthSummary(
            petId = petId,
            petName = pet.name,
            lastCheckupDate = lastCheckup,
            nextVaccinationDue = nextVaccination,
            activeConditions = activeConditions,
            healthStatus = healthStatus
        )
    }

    private suspend fun getLastCheckupDate(petId: Long): Long? {
        val records = medicalRecordDao.getRecentMedicalRecords(petId, 1)
        return records.firstOrNull { it.recordType == MedicalRecordType.EXAMINATION }?.recordDate
    }

    private suspend fun getNextVaccinationDate(petId: Long): Long? {
        val vaccinations = vaccinationDao.getUpcomingVaccinations(petId)
        // This would need to be implemented properly with Flow collection
        return null // Placeholder
    }

    private suspend fun getActiveConditionsCount(petId: Long): Int {
        return medicalRecordDao.getRecordCountByType(petId, MedicalRecordType.DIAGNOSIS)
    }

    private suspend fun calculateHealthStatus(petId: Long): HealthStatus {
        val activeCritical = medicalRecordDao.getActiveMedicalRecords(petId)
        // This would need proper implementation with Flow collection
        return HealthStatus.GOOD // Placeholder
    }

    // Age calculation utilities
    fun calculateAge(dateOfBirth: Long): String {
        val birthDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(dateOfBirth),
            ZoneId.systemDefault()
        )
        val currentDate = LocalDate.now()
        
        val years = ChronoUnit.YEARS.between(birthDate, currentDate)
        val months = ChronoUnit.MONTHS.between(birthDate.plusYears(years), currentDate)
        
        return when {
            years > 0 -> "$years year${if (years > 1) "s" else ""}" + 
                        if (months > 0) " $months month${if (months > 1) "s" else ""}" else ""
            months > 0 -> "$months month${if (months > 1) "s" else ""}"
            else -> {
                val days = ChronoUnit.DAYS.between(birthDate, currentDate)
                "$days day${if (days > 1) "s" else ""}"
            }
        }
    }

    // Vaccination schedule suggestions
    suspend fun getVaccinationRecommendations(petId: Long): List<com.yourorg.vetconnect.viewmodel.VaccinationRecommendation> {
        val pet = getPetById(petId) ?: return emptyList()
        val existingVaccinations = vaccinationDao.getVaccinationsByPet(petId)
        
        // This would contain species-specific vaccination schedules
        return generateVaccinationSchedule(pet.species, pet.dateOfBirth)
    }

    private fun generateVaccinationSchedule(species: PetSpecies, dateOfBirth: Long): List<com.yourorg.vetconnect.viewmodel.VaccinationRecommendation> {
        // Simplified vaccination schedule - in real implementation, this would be comprehensive
        return when (species) {
            PetSpecies.DOG -> listOf(
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("DHPP", "6-8 weeks", true),
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("Rabies", "12-16 weeks", true),
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("Bordetella", "6-8 weeks", false)
            )
            PetSpecies.CAT -> listOf(
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("FVRCP", "6-8 weeks", true),
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("Rabies", "12-16 weeks", true),
                com.yourorg.vetconnect.viewmodel.VaccinationRecommendation("FeLV", "8-12 weeks", false)
            )
            else -> emptyList()
        }
    }

    // Breed-specific health recommendations
    suspend fun getBreedHealthRecommendations(petId: Long): List<com.yourorg.vetconnect.viewmodel.HealthRecommendation> {
        val pet = getPetById(petId) ?: return emptyList()
        return getBreedSpecificRecommendations(pet.breed, pet.species)
    }

    private fun getBreedSpecificRecommendations(breed: String, species: PetSpecies): List<com.yourorg.vetconnect.viewmodel.HealthRecommendation> {
        // This would contain breed-specific health screening recommendations
        return when {
            breed.contains("Golden Retriever", ignoreCase = true) -> listOf(
                com.yourorg.vetconnect.viewmodel.HealthRecommendation("Hip Dysplasia Screening", "Annual", "X-ray examination for hip joint health"),
                com.yourorg.vetconnect.viewmodel.HealthRecommendation("Heart Examination", "Annual", "Echocardiogram for cardiac health"),
                com.yourorg.vetconnect.viewmodel.HealthRecommendation("Eye Examination", "Annual", "CERF examination for inherited eye diseases")
            )
            breed.contains("Persian", ignoreCase = true) && species == PetSpecies.CAT -> listOf(
                com.yourorg.vetconnect.viewmodel.HealthRecommendation("Polycystic Kidney Disease", "Annual", "Ultrasound screening"),
                com.yourorg.vetconnect.viewmodel.HealthRecommendation("Respiratory Monitoring", "Ongoing", "Monitor for breathing difficulties")
            )
            else -> emptyList()
        }
    }
}

// Extension function to convert PetWithOwner to Pet
private fun PetWithOwner.toPet(): Pet {
    return Pet(
        id = this.id,
        ownerId = this.ownerId,
        name = this.name,
        species = this.species,
        breed = this.breed,
        dateOfBirth = this.dateOfBirth,
        gender = this.gender,
        reproductiveStatus = this.reproductiveStatus,
        weight = this.weight,
        height = this.height,
        length = this.length,
        color = this.color,
        markings = this.markings,
        distinguishingFeatures = this.distinguishingFeatures,
        microchipNumber = this.microchipNumber,
        registrationNumber = this.registrationNumber,
        tattoNumber = this.tattoNumber,
        bloodType = this.bloodType,
        allergies = this.allergies,
        chronicConditions = this.chronicConditions,
        currentMedications = this.currentMedications,
        dietaryRestrictions = this.dietaryRestrictions,
        behavioralNotes = this.behavioralNotes,
        emergencyContactName = this.emergencyContactName,
        emergencyContactPhone = this.emergencyContactPhone,
        preferredVeterinarianId = this.preferredVeterinarianId,
        insuranceProvider = this.insuranceProvider,
        insurancePolicyNumber = this.insurancePolicyNumber,
        insuranceExpiryDate = this.insuranceExpiryDate,
        profileImageUrl = this.profileImageUrl,
        additionalNotes = this.additionalNotes,
        isActive = this.isActive,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
