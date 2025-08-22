package com.yourorg.vetconnect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourorg.vetconnect.data.AppRepositoryProvider
import com.yourorg.vetconnect.model.HealthRecord
import com.yourorg.vetconnect.model.HealthRecordType
import com.yourorg.vetconnect.model.Vaccination
import com.yourorg.vetconnect.model.WeightRecord
import com.yourorg.vetconnect.ui.screens.health.HealthRecordFilter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HealthRecordsUiState(
    val isLoading: Boolean = false,
    val petName: String? = null,
    val error: String? = null
)

class HealthRecordsViewModel(
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HealthRecordsUiState())
    val uiState: StateFlow<HealthRecordsUiState> = _uiState.asStateFlow()
    
    private val _healthRecords = MutableStateFlow<List<HealthRecord>>(emptyList())
    val healthRecords: StateFlow<List<HealthRecord>> = _healthRecords.asStateFlow()
    
    private val _vaccinations = MutableStateFlow<List<Vaccination>>(emptyList())
    val vaccinations: StateFlow<List<Vaccination>> = _vaccinations.asStateFlow()
    
    private val _weightRecords = MutableStateFlow<List<WeightRecord>>(emptyList())
    val weightRecords: StateFlow<List<WeightRecord>> = _weightRecords.asStateFlow()
    
    fun loadHealthData(petId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val petRepository = AppRepositoryProvider.getPetRepository(context)
                val database = AppRepositoryProvider.getDatabase(context)
                
                // Load pet info
                val pet = petRepository.getPetById(petId)
                _uiState.value = _uiState.value.copy(petName = pet?.name)
                
                // Start observing health records
                observeHealthRecords(petId)
                
                // Load vaccinations and weight records (mock data for now)
                _vaccinations.value = emptyList()
                _weightRecords.value = emptyList()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown error")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    private fun observeHealthRecords(petId: Long) {
        viewModelScope.launch {
            try {
                val database = AppRepositoryProvider.getDatabase(context)
                database.healthRecordDao().getHealthRecordsByPet(petId).collect { records ->
                    _healthRecords.value = records
                }
            } catch (e: Exception) {
                // If health records fail, use empty list
                _healthRecords.value = emptyList()
            }
        }
    }
    
    fun addHealthRecord(healthRecord: HealthRecord) {
        viewModelScope.launch {
            try {
                val database = AppRepositoryProvider.getDatabase(context)
                database.healthRecordDao().insertHealthRecord(healthRecord)
                // Reload data
                loadHealthData(healthRecord.petId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteHealthRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                val database = AppRepositoryProvider.getDatabase(context)
                val record = database.healthRecordDao().getHealthRecordById(recordId)
                record?.let {
                    database.healthRecordDao().deleteHealthRecord(it)
                    // Update local state
                    val currentRecords = _healthRecords.value
                    _healthRecords.value = currentRecords.filter { rec -> rec.id != recordId }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun getRecordCounts(records: List<HealthRecord>): Map<HealthRecordFilter, Int> {
        return mapOf(
            HealthRecordFilter.ALL to records.size,
            HealthRecordFilter.VACCINATIONS to records.count { it.recordType == HealthRecordType.VACCINATION },
            HealthRecordFilter.TREATMENTS to records.count { it.recordType == HealthRecordType.TREATMENT },
            HealthRecordFilter.CHECKUPS to records.count { it.recordType == HealthRecordType.CHECKUP },
            HealthRecordFilter.SURGERIES to records.count { it.recordType == HealthRecordType.SURGERY },
            HealthRecordFilter.MEDICATIONS to records.count { it.recordType == HealthRecordType.MEDICATION }
        )
    }
    
    fun getUpcomingVaccinations(vaccinations: List<Vaccination>): List<Vaccination> {
        val now = LocalDate.now()
        return vaccinations.filter { vaccination ->
            vaccination.nextDueDate?.let { dueDate ->
                val dueDateLocal = java.time.Instant.ofEpochMilli(dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val daysUntil = ChronoUnit.DAYS.between(now, dueDateLocal)
                daysUntil in 0..30 // Due within next 30 days
            } ?: false
        }
    }
    
    fun getOverdueItems(healthRecords: List<HealthRecord>): List<HealthRecord> {
        val now = LocalDate.now()
        return healthRecords.filter { record ->
            record.nextDueDate?.let { dueDate ->
                val dueDateLocal = java.time.Instant.ofEpochMilli(dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                dueDateLocal.isBefore(now)
            } ?: false
        }
    }
    
    fun getHealthSummary(petId: Long): HealthSummary {
        val records = _healthRecords.value
        val vaccinations = _vaccinations.value
        val weights = _weightRecords.value
        
        val recentRecordsCount = records.count { record ->
            val recordDate = java.time.Instant.ofEpochMilli(record.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            ChronoUnit.DAYS.between(recordDate, LocalDate.now()) <= 30
        }
        
        val upcomingVaccinationsCount = getUpcomingVaccinations(vaccinations).size
        val overdueItemsCount = getOverdueItems(records).size
        
        val currentWeight = weights.maxByOrNull { it.recordDate }?.weight
        val previousWeight = if (weights.size >= 2) {
            weights.sortedByDescending { it.recordDate }[1].weight
        } else null
        
        return HealthSummary(
            recentRecordsCount = recentRecordsCount,
            upcomingVaccinationsCount = upcomingVaccinationsCount,
            overdueItemsCount = overdueItemsCount,
            currentWeight = currentWeight,
            previousWeight = previousWeight,
            totalRecordsCount = records.size,
            lastCheckupDate = records
                .filter { it.recordType == HealthRecordType.CHECKUP }
                .maxByOrNull { it.date }?.date
        )
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HealthRecordsViewModel::class.java)) {
                return HealthRecordsViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class HealthSummary(
    val recentRecordsCount: Int,
    val upcomingVaccinationsCount: Int,
    val overdueItemsCount: Int,
    val currentWeight: Double?,
    val previousWeight: Double?,
    val totalRecordsCount: Int,
    val lastCheckupDate: Long?
)


