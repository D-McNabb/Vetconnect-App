package com.example.vetconnect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetconnect.data.model.*
import com.example.vetconnect.data.repository.PetRepository
import com.example.vetconnect.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {
    
    private val _petsState = MutableStateFlow<UiState<List<Pet>>>(UiState.Loading)
    val petsState: StateFlow<UiState<List<Pet>>> = _petsState.asStateFlow()
    
    private val _petState = MutableStateFlow<UiState<Pet>>(UiState.Loading)
    val petState: StateFlow<UiState<Pet>> = _petState.asStateFlow()
    
    private val _createPetState = MutableStateFlow<UiState<Pet>>(UiState.Success(null))
    val createPetState: StateFlow<UiState<Pet>> = _createPetState.asStateFlow()
    
    private val _updatePetState = MutableStateFlow<UiState<Pet>>(UiState.Success(null))
    val updatePetState: StateFlow<UiState<Pet>> = _updatePetState.asStateFlow()
    
    private val _deletePetState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val deletePetState: StateFlow<UiState<Unit>> = _deletePetState.asStateFlow()
    
    fun loadPets(ownerId: String? = null) {
        _petsState.value = UiState.Loading
        
        viewModelScope.launch {
            petRepository.getPets(ownerId).collect { result ->
                _petsState.value = when {
                    result.isSuccess -> UiState.Success(result.getOrNull() ?: emptyList())
                    result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load pets")
                }
            }
        }
    }
    
    fun loadPet(petId: String) {
        _petState.value = UiState.Loading
        
        viewModelScope.launch {
            val result = petRepository.getPetById(petId)
            _petState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load pet")
            }
        }
    }
    
    fun createPet(
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        gender: PetGender,
        color: String,
        weight: Double,
        microchipId: String? = null
    ) {
        if (name.isBlank() || species.isBlank() || breed.isBlank() || dateOfBirth.isBlank() || color.isBlank()) {
            _createPetState.value = UiState.Error("All required fields must be filled")
            return
        }
        
        if (weight <= 0) {
            _createPetState.value = UiState.Error("Weight must be greater than 0")
            return
        }
        
        _createPetState.value = UiState.Loading
        
        viewModelScope.launch {
            val request = CreatePetRequest(
                name = name,
                species = species,
                breed = breed,
                dateOfBirth = dateOfBirth,
                gender = gender,
                color = color,
                weight = weight,
                microchipId = microchipId
            )
            
            val result = petRepository.createPet(request)
            _createPetState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to create pet")
            }
        }
    }
    
    fun updatePet(
        petId: String,
        name: String? = null,
        species: String? = null,
        breed: String? = null,
        dateOfBirth: String? = null,
        gender: PetGender? = null,
        color: String? = null,
        weight: Double? = null,
        microchipId: String? = null
    ) {
        _updatePetState.value = UiState.Loading
        
        viewModelScope.launch {
            val request = UpdatePetRequest(
                name = name,
                species = species,
                breed = breed,
                dateOfBirth = dateOfBirth,
                gender = gender,
                color = color,
                weight = weight,
                microchipId = microchipId
            )
            
            val result = petRepository.updatePet(petId, request)
            _updatePetState.value = when {
                result.isSuccess -> UiState.Success(result.getOrNull()!!)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to update pet")
            }
        }
    }
    
    fun deletePet(petId: String) {
        _deletePetState.value = UiState.Loading
        
        viewModelScope.launch {
            val result = petRepository.deletePet(petId)
            _deletePetState.value = when {
                result.isSuccess -> UiState.Success(Unit)
                result.isFailure -> UiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete pet")
            }
        }
    }
    
    fun refreshPets(ownerId: String? = null) {
        viewModelScope.launch {
            petRepository.refreshPets(ownerId)
        }
    }
    
    fun resetCreatePetState() {
        _createPetState.value = UiState.Success(null)
    }
    
    fun resetUpdatePetState() {
        _updatePetState.value = UiState.Success(null)
    }
    
    fun resetDeletePetState() {
        _deletePetState.value = UiState.Success(Unit)
    }
} 