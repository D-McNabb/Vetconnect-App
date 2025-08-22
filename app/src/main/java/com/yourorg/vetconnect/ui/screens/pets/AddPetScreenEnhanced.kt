package com.yourorg.vetconnect.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.ui.components.*
import com.yourorg.vetconnect.viewmodel.PetViewModel
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreenEnhanced(navController: NavController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(
        factory = PetViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Form state
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf<PetSpecies?>(null) }
    var breed by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<LocalDate?>(null) }
    var gender by remember { mutableStateOf<PetGender?>(null) }
    var reproductiveStatus by remember { mutableStateOf<ReproductiveStatus?>(null) }
    var weight by remember { mutableStateOf<Double?>(null) }
    var height by remember { mutableStateOf<Double?>(null) }
    var length by remember { mutableStateOf<Double?>(null) }
    var color by remember { mutableStateOf("") }
    var markings by remember { mutableStateOf("") }
    var distinguishingFeatures by remember { mutableStateOf("") }
    var microchipNumber by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var tattoNumber by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf<List<String>>(emptyList()) }
    var chronicConditions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentMedications by remember { mutableStateOf<List<String>>(emptyList()) }
    var dietaryRestrictions by remember { mutableStateOf<List<String>>(emptyList()) }
    var behavioralNotes by remember { mutableStateOf("") }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var insuranceProvider by remember { mutableStateOf("") }
    var insurancePolicyNumber by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // Validation
    val isFormValid = name.isNotBlank() && 
                     species != null && 
                     breed.isNotBlank() && 
                     dateOfBirth != null && 
                     gender != null && 
                     reproductiveStatus != null && 
                     weight != null && 
                     color.isNotBlank()

    LaunchedEffect(uiState.createdPetId) {
        if (uiState.createdPetId != null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pet", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                val pet = Pet(
                                    ownerId = 1, // Should be dynamic based on current user
                                    name = name.trim(),
                                    species = species!!,
                                    breed = breed.trim(),
                                    dateOfBirth = dateOfBirth!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                    gender = gender!!,
                                    reproductiveStatus = reproductiveStatus!!,
                                    weight = weight!!,
                                    height = height,
                                    length = length,
                                    color = color.trim(),
                                    markings = markings.takeIf { it.isNotBlank() },
                                    distinguishingFeatures = distinguishingFeatures.takeIf { it.isNotBlank() },
                                    microchipNumber = microchipNumber.takeIf { it.isNotBlank() },
                                    registrationNumber = registrationNumber.takeIf { it.isNotBlank() },
                                    tattoNumber = tattoNumber.takeIf { it.isNotBlank() },
                                    bloodType = bloodType.takeIf { it.isNotBlank() },
                                    allergies = allergies,
                                    chronicConditions = chronicConditions,
                                    currentMedications = currentMedications,
                                    dietaryRestrictions = dietaryRestrictions,
                                    behavioralNotes = behavioralNotes.takeIf { it.isNotBlank() },
                                    emergencyContactName = emergencyContactName.takeIf { it.isNotBlank() },
                                    emergencyContactPhone = emergencyContactPhone.takeIf { it.isNotBlank() },
                                    insuranceProvider = insuranceProvider.takeIf { it.isNotBlank() },
                                    insurancePolicyNumber = insurancePolicyNumber.takeIf { it.isNotBlank() },
                                    additionalNotes = additionalNotes.takeIf { it.isNotBlank() }
                                )
                                viewModel.createPet(pet)
                            }
                        },
                        enabled = isFormValid && !uiState.isCreating
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ClinicalSection(
                    title = "Basic Information",
                    subtitle = "Essential details about your pet",
                    isRequired = true
                ) {
                    ClinicalTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Pet Name",
                        placeholder = "Enter your pet's name",
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalDropdown(
                        value = species,
                        onValueChange = { species = it },
                        options = PetSpecies.values().toList(),
                        label = "Species",
                        optionLabel = { it.name.replace("_", " ") },
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = "Breed",
                        placeholder = "Enter breed",
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClinicalDropdown(
                            value = gender,
                            onValueChange = { gender = it },
                            options = PetGender.values().toList(),
                            label = "Gender",
                            optionLabel = { it.name },
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        ClinicalDropdown(
                            value = reproductiveStatus,
                            onValueChange = { reproductiveStatus = it },
                            options = ReproductiveStatus.values().toList(),
                            label = "Reproductive Status",
                            optionLabel = { it.name },
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalDatePicker(
                        selectedDate = dateOfBirth,
                        onDateSelected = { dateOfBirth = it },
                        label = "Date of Birth",
                        isRequired = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                ClinicalSection(
                    title = "Physical Characteristics",
                    subtitle = "Physical attributes and measurements"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeightInput(
                            weight = weight,
                            onWeightChange = { weight = it },
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        WeightInput(
                            weight = height,
                            onWeightChange = { height = it },
                            unit = "cm",
                            label = "Height",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeightInput(
                            weight = length,
                            onWeightChange = { length = it },
                            unit = "cm",
                            label = "Length",
                            modifier = Modifier.weight(1f)
                        )
                        
                        ClinicalTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = "Color",
                            placeholder = "Primary color",
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalTextField(
                        value = markings,
                        onValueChange = { markings = it },
                        label = "Markings",
                        placeholder = "Describe any markings",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalTextField(
                        value = distinguishingFeatures,
                        onValueChange = { distinguishingFeatures = it },
                        label = "Distinguishing Features",
                        placeholder = "Any unique characteristics",
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                ClinicalSection(
                    title = "Identification",
                    subtitle = "Microchip, registration, and identification numbers"
                ) {
                    ClinicalTextField(
                        value = microchipNumber,
                        onValueChange = { microchipNumber = it },
                        label = "Microchip Number",
                        placeholder = "15-digit microchip number",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClinicalTextField(
                            value = registrationNumber,
                            onValueChange = { registrationNumber = it },
                            label = "Registration Number",
                            placeholder = "Breed registration",
                            modifier = Modifier.weight(1f)
                        )
                        
                        ClinicalTextField(
                            value = tattoNumber,
                            onValueChange = { tattoNumber = it },
                            label = "Tattoo Number",
                            placeholder = "Identification tattoo",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                ClinicalSection(
                    title = "Medical Information",
                    subtitle = "Health conditions, allergies, and medical history"
                ) {
                    ClinicalTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = "Blood Type",
                        placeholder = "If known",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DynamicList(
                        items = allergies,
                        onItemsChange = { allergies = it },
                        label = "Allergies",
                        placeholder = "Enter allergy",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DynamicList(
                        items = chronicConditions,
                        onItemsChange = { chronicConditions = it },
                        label = "Chronic Conditions",
                        placeholder = "Enter condition",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DynamicList(
                        items = currentMedications,
                        onItemsChange = { currentMedications = it },
                        label = "Current Medications",
                        placeholder = "Enter medication",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DynamicList(
                        items = dietaryRestrictions,
                        onItemsChange = { dietaryRestrictions = it },
                        label = "Dietary Restrictions",
                        placeholder = "Enter restriction",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClinicalTextField(
                        value = behavioralNotes,
                        onValueChange = { behavioralNotes = it },
                        label = "Behavioral Notes",
                        placeholder = "Temperament, special handling requirements, etc.",
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                ClinicalSection(
                    title = "Emergency & Insurance",
                    subtitle = "Emergency contacts and insurance information"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClinicalTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = "Emergency Contact Name",
                            placeholder = "Full name",
                            modifier = Modifier.weight(1f)
                        )
                        
                        ClinicalTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = "Emergency Contact Phone",
                            placeholder = "Phone number",
                            keyboardType = KeyboardType.Phone,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ClinicalTextField(
                            value = insuranceProvider,
                            onValueChange = { insuranceProvider = it },
                            label = "Insurance Provider",
                            placeholder = "Pet insurance company",
                            modifier = Modifier.weight(1f)
                        )
                        
                        ClinicalTextField(
                            value = insurancePolicyNumber,
                            onValueChange = { insurancePolicyNumber = it },
                            label = "Policy Number",
                            placeholder = "Insurance policy ID",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            item {
                ClinicalSection(
                    title = "Additional Notes",
                    subtitle = "Any other important information"
                ) {
                    ClinicalTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = "Additional Notes",
                        placeholder = "Any other important information about your pet",
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Handle success/error messages
    LaunchedEffect(uiState.successMessage, uiState.error) {
        // In a real app, you would show snackbar or toast messages here
    }
}
