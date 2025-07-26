package com.example.vetconnect.data.repository

import com.example.vetconnect.data.api.VetConnectApiService
import com.example.vetconnect.data.local.PetDao
import com.example.vetconnect.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val apiService: VetConnectApiService,
    private val petDao: PetDao
) {
    
    fun getPets(ownerId: String? = null): Flow<Result<List<Pet>>> = flow {
        try {
            // First emit cached data
            val cachedPets = if (ownerId != null) {
                petDao.getPetsByOwner(ownerId)
            } else {
                petDao.getAllPets()
            }
            emit(Result.success(cachedPets))
            
            // Then fetch from API
            val response = apiService.getPets(ownerId = ownerId)
            if (response.success) {
                petDao.insertPets(response.data)
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun getPetById(petId: String): Result<Pet> {
        return try {
            // First try to get from cache
            val cachedPet = petDao.getPetById(petId)
            if (cachedPet != null) {
                return Result.success(cachedPet)
            }
            
            // If not in cache, fetch from API
            val response = apiService.getPet(petId)
            if (response.success && response.data != null) {
                petDao.insertPet(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPet(request: CreatePetRequest): Result<Pet> {
        return try {
            val response = apiService.createPet(request)
            if (response.success && response.data != null) {
                petDao.insertPet(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePet(petId: String, request: UpdatePetRequest): Result<Pet> {
        return try {
            val response = apiService.updatePet(petId, request)
            if (response.success && response.data != null) {
                petDao.insertPet(response.data)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePet(petId: String): Result<Unit> {
        return try {
            val response = apiService.deletePet(petId)
            if (response.success) {
                // Remove from cache
                val pet = petDao.getPetById(petId)
                if (pet != null) {
                    petDao.deletePet(pet)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshPets(ownerId: String? = null) {
        try {
            val response = apiService.getPets(ownerId = ownerId)
            if (response.success) {
                petDao.insertPets(response.data)
            }
        } catch (e: Exception) {
            // Handle error silently for background refresh
        }
    }
    
    suspend fun clearCache() {
        petDao.deleteAllPets()
    }
} 