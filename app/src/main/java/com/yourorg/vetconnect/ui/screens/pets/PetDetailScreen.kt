package com.yourorg.vetconnect.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.navigation.Screen
import com.yourorg.vetconnect.ui.components.ClinicalSection
import com.yourorg.vetconnect.viewmodel.PetViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    navController: NavController,
    petId: Long
) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(
        factory = PetViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()

    LaunchedEffect(petId) {
        viewModel.loadPetProfile(petId)
        viewModel.loadMedicalRecords(petId)
        viewModel.loadVaccinations(petId)
        viewModel.loadWeightRecords(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedPet?.name ?: "Pet Details", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navigate to edit screen */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoadingProfile -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.loadPetProfile(petId) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            selectedPet != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PetBasicInfoCard(selectedPet!!, viewModel)
                    }
                    
                    item {
                        PetPhysicalCharacteristicsCard(selectedPet!!)
                    }
                    
                    item {
                        PetMedicalInfoCard(selectedPet!!)
                    }
                    
                    item {
                        PetRecentRecordsCard(
                            medicalRecords = uiState.medicalRecords.take(5),
                            vaccinations = uiState.vaccinations.take(3),
                            weightRecords = uiState.weightRecords.take(3),
                            onViewAllHealthRecords = {
                                navController.navigate(Screen.HealthRecords.createRoute(petId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetBasicInfoCard(
    pet: Pet,
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    ClinicalSection(
        title = "Basic Information",
        modifier = modifier
    ) {
        InfoRow("Name", pet.name)
        InfoRow("Species", pet.species.name.replace("_", " "))
        InfoRow("Breed", pet.breed)
        InfoRow("Age", viewModel.calculateAge(pet.dateOfBirth))
        InfoRow("Gender", pet.gender.name)
        InfoRow("Reproductive Status", pet.reproductiveStatus.name)
        
        pet.microchipNumber?.let {
            InfoRow("Microchip", it)
        }
    }
}

@Composable
fun PetPhysicalCharacteristicsCard(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    ClinicalSection(
        title = "Physical Characteristics",
        modifier = modifier
    ) {
        InfoRow("Weight", "${pet.weight} kg")
        pet.height?.let { InfoRow("Height", "$it cm") }
        pet.length?.let { InfoRow("Length", "$it cm") }
        InfoRow("Color", pet.color)
        pet.markings?.let { InfoRow("Markings", it) }
        pet.distinguishingFeatures?.let { InfoRow("Distinguishing Features", it) }
    }
}

@Composable
fun PetMedicalInfoCard(
    pet: Pet,
    modifier: Modifier = Modifier
) {
    ClinicalSection(
        title = "Medical Information",
        modifier = modifier
    ) {
        pet.bloodType?.let { InfoRow("Blood Type", it) }
        
        if (pet.allergies.isNotEmpty()) {
            InfoRow("Allergies", pet.allergies.joinToString(", "))
        }
        
        if (pet.chronicConditions.isNotEmpty()) {
            InfoRow("Chronic Conditions", pet.chronicConditions.joinToString(", "))
        }
        
        if (pet.currentMedications.isNotEmpty()) {
            InfoRow("Current Medications", pet.currentMedications.joinToString(", "))
        }
        
        if (pet.dietaryRestrictions.isNotEmpty()) {
            InfoRow("Dietary Restrictions", pet.dietaryRestrictions.joinToString(", "))
        }
        
        pet.behavioralNotes?.let { InfoRow("Behavioral Notes", it) }
    }
}

@Composable
fun PetRecentRecordsCard(
    medicalRecords: List<MedicalRecord>,
    vaccinations: List<Vaccination>,
    weightRecords: List<WeightRecord>,
    onViewAllHealthRecords: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ClinicalSection(
        title = "Recent Records",
        subtitle = "Latest medical records, vaccinations, and weight measurements",
        modifier = modifier
    ) {
        if (medicalRecords.isNotEmpty()) {
            Text(
                text = "Recent Medical Records",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            medicalRecords.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDate(record.recordDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = record.recordType.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        if (vaccinations.isNotEmpty()) {
            Text(
                text = "Recent Vaccinations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            vaccinations.forEach { vaccination ->
                InfoRow(vaccination.vaccineName, formatDate(vaccination.administeredDate))
            }
        }
        
        if (weightRecords.isNotEmpty()) {
            Text(
                text = "Weight History",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            weightRecords.forEach { record ->
                InfoRow(
                    formatDate(record.recordDate),
                    "${record.weight} kg"
                )
            }
        }
        
        // View All Records Button
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onViewAllHealthRecords,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View All Health Records")
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f)
        )
    }
}

fun formatDate(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
