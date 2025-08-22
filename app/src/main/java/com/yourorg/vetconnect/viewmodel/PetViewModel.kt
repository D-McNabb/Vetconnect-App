package com.yourorg.vetconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourorg.vetconnect.data.AppRepositoryProvider
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class PetViewModel(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetUiState())
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    init {
        loadPets()
    }

    fun loadPets(ownerId: Long = 1) { // Default owner ID - should be dynamic
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                petRepository.getPetsByOwner(ownerId).collect { pets ->
                    _uiState.value = _uiState.value.copy(
                        pets = pets,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load pets"
                )
            }
        }
    }

    fun loadPetProfile(petId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingProfile = true)
                
                val profile = petRepository.getPetProfile(petId)
                if (profile != null) {
                    _selectedPet.value = profile.pet
                    _uiState.value = _uiState.value.copy(
                        selectedPetProfile = profile,
                        isLoadingProfile = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingProfile = false,
                        error = "Pet not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProfile = false,
                    error = e.message ?: "Failed to load pet profile"
                )
            }
        }
    }

    fun createPet(pet: Pet) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreating = true)
                
                val petId = petRepository.createPet(pet)
                
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    createdPetId = petId,
                    successMessage = "Pet created successfully!"
                )
                
                // Reload pets list
                loadPets(pet.ownerId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message ?: "Failed to create pet"
                )
            }
        }
    }

    fun updatePet(pet: Pet) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdating = true)
                
                petRepository.updatePet(pet)
                
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    successMessage = "Pet updated successfully!"
                )
                
                // Reload pets list
                loadPets(pet.ownerId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = e.message ?: "Failed to update pet"
                )
            }
        }
    }

    fun deletePet(petId: Long, ownerId: Long) {
        viewModelScope.launch {
            try {
                petRepository.deletePet(petId)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = "Pet deleted successfully!"
                )
                
                // Reload pets list
                loadPets(ownerId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete pet"
                )
            }
        }
    }

    fun searchPets(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSearching = true)
                
                val searchResults = petRepository.searchPets(query)
                
                _uiState.value = _uiState.value.copy(
                    searchResults = searchResults,
                    isSearching = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun filterPetsBySpecies(species: PetSpecies) {
        viewModelScope.launch {
            try {
                val filteredPets = petRepository.getPetsBySpecies(species)
                _uiState.value = _uiState.value.copy(
                    filteredPets = filteredPets
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Filter failed"
                )
            }
        }
    }

    // Medical Records Management
    fun loadMedicalRecords(petId: Long) {
        viewModelScope.launch {
            try {
                petRepository.getMedicalRecordsByPet(petId).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        medicalRecords = records
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load medical records"
                )
            }
        }
    }

    fun addMedicalRecord(record: MedicalRecord) {
        viewModelScope.launch {
            try {
                petRepository.addMedicalRecord(record)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Medical record added successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add medical record"
                )
            }
        }
    }

    // Vaccination Management
    fun loadVaccinations(petId: Long) {
        viewModelScope.launch {
            try {
                petRepository.getVaccinationsByPet(petId).collect { vaccinations ->
                    _uiState.value = _uiState.value.copy(
                        vaccinations = vaccinations
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load vaccinations"
                )
            }
        }
    }

    fun addVaccination(vaccination: Vaccination) {
        viewModelScope.launch {
            try {
                petRepository.addVaccination(vaccination)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Vaccination record added successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add vaccination"
                )
            }
        }
    }

    // Weight Management
    fun loadWeightRecords(petId: Long) {
        viewModelScope.launch {
            try {
                petRepository.getWeightRecordsByPet(petId).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        weightRecords = records
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load weight records"
                )
            }
        }
    }

    fun addWeightRecord(record: WeightRecord) {
        viewModelScope.launch {
            try {
                petRepository.addWeightRecord(record)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Weight record added successfully!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add weight record"
                )
            }
        }
    }

    // Health Analytics
    fun loadHealthSummary(petId: Long) {
        viewModelScope.launch {
            try {
                val summary = petRepository.getPetHealthSummary(petId)
                _uiState.value = _uiState.value.copy(
                    healthSummary = summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load health summary"
                )
            }
        }
    }

    fun loadVaccinationRecommendations(petId: Long) {
        viewModelScope.launch {
            try {
                val recommendations = petRepository.getVaccinationRecommendations(petId)
                _uiState.value = _uiState.value.copy(
                    vaccinationRecommendations = recommendations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load vaccination recommendations"
                )
            }
        }
    }

    fun loadBreedHealthRecommendations(petId: Long) {
        viewModelScope.launch {
            try {
                val recommendations = petRepository.getBreedHealthRecommendations(petId)
                _uiState.value = _uiState.value.copy(
                    breedHealthRecommendations = recommendations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load breed health recommendations"
                )
            }
        }
    }

    // Utility functions
    fun calculateAge(dateOfBirth: Long): String {
        return petRepository.calculateAge(dateOfBirth)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            filteredPets = emptyList()
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PetViewModel(
                    AppRepositoryProvider.getPetRepository(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class PetUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPetProfile: PetProfile? = null,
    val medicalRecords: List<MedicalRecord> = emptyList(),
    val vaccinations: List<Vaccination> = emptyList(),
    val weightRecords: List<WeightRecord> = emptyList(),
    val searchResults: List<Pet> = emptyList(),
    val filteredPets: List<Pet> = emptyList(),
    val healthSummary: PetHealthSummary? = null,
    val vaccinationRecommendations: List<VaccinationRecommendation> = emptyList(),
    val breedHealthRecommendations: List<HealthRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingProfile: Boolean = false,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isSearching: Boolean = false,
    val createdPetId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null
)

data class VaccinationRecommendation(
    val vaccineName: String,
    val recommendedAge: String,
    val isCore: Boolean
)

data class HealthRecommendation(
    val screeningName: String,
    val frequency: String,
    val description: String
)
