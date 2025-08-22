package com.yourorg.vetconnect.ui.screens.pets

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.ui.components.*
import com.yourorg.vetconnect.viewmodel.PetViewModel
import java.time.LocalDate
import java.time.ZoneId

enum class AddPetStep(val title: String, val subtitle: String, val icon: ImageVector) {
    BASIC_INFO("Basic Information", "Tell us about your pet", Icons.Default.Person),
    PHYSICAL_DETAILS("Physical Details", "Measurements and characteristics", Icons.Default.Edit),
    IDENTIFICATION("Identification", "Microchip and registration", Icons.Default.Star),
    MEDICAL_INFO("Medical Information", "Health conditions and allergies", Icons.Default.Add),
    EMERGENCY_CONTACTS("Emergency & Insurance", "Contact and insurance details", Icons.Default.Phone),
    REVIEW("Review & Save", "Confirm all information", Icons.Default.CheckCircle)
}

data class FormValidation(
    val isValid: Boolean = false,
    val errors: Map<String, String> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ImprovedAddPetScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(
        factory = PetViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(AddPetStep.BASIC_INFO) }
    var canProceed by remember { mutableStateOf(false) }
    
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
    val validation = remember(currentStep, name, species, breed, dateOfBirth, gender, reproductiveStatus, weight, color) {
        validateCurrentStep(
            currentStep, name, species, breed, dateOfBirth, gender, reproductiveStatus, weight, color,
            emergencyContactName, emergencyContactPhone
        )
    }
    
    LaunchedEffect(validation) {
        canProceed = validation.isValid
    }

    LaunchedEffect(uiState.createdPetId) {
        if (uiState.createdPetId != null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Add New Pet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentStep.title,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentStep == AddPetStep.BASIC_INFO) {
                            navController.popBackStack()
                        } else {
                            val currentIndex = AddPetStep.values().indexOf(currentStep)
                            if (currentIndex > 0) {
                                currentStep = AddPetStep.values()[currentIndex - 1]
                            }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentStep = currentStep,
                canProceed = canProceed,
                isLoading = uiState.isCreating,
                onPrevious = {
                    val currentIndex = AddPetStep.values().indexOf(currentStep)
                    if (currentIndex > 0) {
                        currentStep = AddPetStep.values()[currentIndex - 1]
                    }
                },
                onNext = {
                    val currentIndex = AddPetStep.values().indexOf(currentStep)
                    if (currentIndex < AddPetStep.values().size - 1) {
                        currentStep = AddPetStep.values()[currentIndex + 1]
                    }
                },
                onSave = {
                    if (validation.isValid) {
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress Indicator
            StepProgressIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(16.dp)
            )
            
            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState.ordinal > initialState.ordinal) 1000 else -1000 }
                    ) + fadeIn() with slideOutHorizontally(
                        targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -1000 else 1000 }
                    ) + fadeOut()
                },
                modifier = Modifier.weight(1f)
            ) { step ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        StepHeader(step)
                    }
                    
                    item {
                        when (step) {
                            AddPetStep.BASIC_INFO -> BasicInfoStep(
                                name = name,
                                onNameChange = { name = it },
                                species = species,
                                onSpeciesChange = { species = it },
                                breed = breed,
                                onBreedChange = { breed = it },
                                dateOfBirth = dateOfBirth,
                                onDateOfBirthChange = { dateOfBirth = it },
                                gender = gender,
                                onGenderChange = { gender = it },
                                reproductiveStatus = reproductiveStatus,
                                onReproductiveStatusChange = { reproductiveStatus = it },
                                validation = validation
                            )
                            
                            AddPetStep.PHYSICAL_DETAILS -> PhysicalDetailsStep(
                                weight = weight,
                                onWeightChange = { weight = it },
                                height = height,
                                onHeightChange = { height = it },
                                length = length,
                                onLengthChange = { length = it },
                                color = color,
                                onColorChange = { color = it },
                                markings = markings,
                                onMarkingsChange = { markings = it },
                                distinguishingFeatures = distinguishingFeatures,
                                onDistinguishingFeaturesChange = { distinguishingFeatures = it },
                                validation = validation
                            )
                            
                            AddPetStep.IDENTIFICATION -> IdentificationStep(
                                microchipNumber = microchipNumber,
                                onMicrochipNumberChange = { microchipNumber = it },
                                registrationNumber = registrationNumber,
                                onRegistrationNumberChange = { registrationNumber = it },
                                tattoNumber = tattoNumber,
                                onTattoNumberChange = { tattoNumber = it }
                            )
                            
                            AddPetStep.MEDICAL_INFO -> MedicalInfoStep(
                                bloodType = bloodType,
                                onBloodTypeChange = { bloodType = it },
                                allergies = allergies,
                                onAllergiesChange = { allergies = it },
                                chronicConditions = chronicConditions,
                                onChronicConditionsChange = { chronicConditions = it },
                                currentMedications = currentMedications,
                                onCurrentMedicationsChange = { currentMedications = it },
                                dietaryRestrictions = dietaryRestrictions,
                                onDietaryRestrictionsChange = { dietaryRestrictions = it },
                                behavioralNotes = behavioralNotes,
                                onBehavioralNotesChange = { behavioralNotes = it }
                            )
                            
                            AddPetStep.EMERGENCY_CONTACTS -> EmergencyContactsStep(
                                emergencyContactName = emergencyContactName,
                                onEmergencyContactNameChange = { emergencyContactName = it },
                                emergencyContactPhone = emergencyContactPhone,
                                onEmergencyContactPhoneChange = { emergencyContactPhone = it },
                                insuranceProvider = insuranceProvider,
                                onInsuranceProviderChange = { insuranceProvider = it },
                                insurancePolicyNumber = insurancePolicyNumber,
                                onInsurancePolicyNumberChange = { insurancePolicyNumber = it },
                                validation = validation
                            )
                            
                            AddPetStep.REVIEW -> ReviewStep(
                                name = name,
                                species = species,
                                breed = breed,
                                dateOfBirth = dateOfBirth,
                                gender = gender,
                                reproductiveStatus = reproductiveStatus,
                                weight = weight,
                                color = color,
                                allergies = allergies,
                                chronicConditions = chronicConditions,
                                emergencyContactName = emergencyContactName,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Show error messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, show snackbar here
        }
    }
}

@Composable
fun StepHeader(step: AddPetStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(6.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StepProgressIndicator(
    currentStep: AddPetStep,
    modifier: Modifier = Modifier
) {
    val steps = AddPetStep.values()
    val currentIndex = steps.indexOf(currentStep)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex
            
            // Step circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    isCurrent -> Text(
                        text = "${index + 1}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    else -> Text(
                        text = "${index + 1}",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            // Connector line
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .background(
                            if (index < currentIndex) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentStep: AddPetStep,
    canProceed: Boolean,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button
            if (currentStep != AddPetStep.BASIC_INFO) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(100.dp))
            }
            
            // Next/Save button
            Button(
                onClick = if (currentStep == AddPetStep.REVIEW) onSave else onNext,
                enabled = canProceed && !isLoading,
                modifier = Modifier.width(120.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        if (currentStep == AddPetStep.REVIEW) "Save Pet" else "Next"
                    )
                }
            }
        }
    }
}

// Validation function
fun validateCurrentStep(
    step: AddPetStep,
    name: String,
    species: PetSpecies?,
    breed: String,
    dateOfBirth: LocalDate?,
    gender: PetGender?,
    reproductiveStatus: ReproductiveStatus?,
    weight: Double?,
    color: String,
    emergencyContactName: String,
    emergencyContactPhone: String
): FormValidation {
    val errors = mutableMapOf<String, String>()
    
    when (step) {
        AddPetStep.BASIC_INFO -> {
            if (name.isBlank()) errors["name"] = "Pet name is required"
            if (species == null) errors["species"] = "Please select a species"
            if (breed.isBlank()) errors["breed"] = "Breed is required"
            if (dateOfBirth == null) errors["dateOfBirth"] = "Date of birth is required"
            if (gender == null) errors["gender"] = "Please select gender"
            if (reproductiveStatus == null) errors["reproductiveStatus"] = "Please select reproductive status"
        }
        
        AddPetStep.PHYSICAL_DETAILS -> {
            if (weight == null || weight <= 0) errors["weight"] = "Valid weight is required"
            if (color.isBlank()) errors["color"] = "Color is required"
        }
        
        AddPetStep.EMERGENCY_CONTACTS -> {
            if (emergencyContactName.isNotBlank() && emergencyContactPhone.isBlank()) {
                errors["emergencyContactPhone"] = "Phone number required when contact name is provided"
            }
            if (emergencyContactPhone.isNotBlank() && emergencyContactName.isBlank()) {
                errors["emergencyContactName"] = "Contact name required when phone number is provided"
            }
        }
        
        else -> {
            // Other steps are optional
        }
    }
    
    return FormValidation(
        isValid = errors.isEmpty(),
        errors = errors
    )
}
