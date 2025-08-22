package com.yourorg.vetconnect.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.ui.components.CalendarView
import com.yourorg.vetconnect.ui.components.CalendarViewMode
import com.yourorg.vetconnect.viewmodel.SimpleAppointmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: SimpleAppointmentViewModel = viewModel(
        factory = SimpleAppointmentViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    var currentStep by remember { mutableStateOf(BookingStep.SELECT_PET) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var selectedVeterinarian by remember { mutableStateOf<Long?>(null) }
    var selectedSlot by remember { mutableStateOf<AvailableTimeSlot?>(null) }
    var appointmentType by remember { mutableStateOf(AppointmentType.ROUTINE_CHECKUP) }
    var urgencyLevel by remember { mutableStateOf(UrgencyLevel.MEDIUM) }
    var reason by remember { mutableStateOf("") }

    // Mock pets data
    val pets = remember {
        listOf(
            Pet(
                id = 1,
                ownerId = 1,
                name = "Buddy",
                species = PetSpecies.DOG,
                breed = "Golden Retriever",
                dateOfBirth = System.currentTimeMillis(),
                gender = PetGender.MALE,
                reproductiveStatus = ReproductiveStatus.NEUTERED,
                weight = 25.0,
                color = "Golden"
            ),
            Pet(
                id = 2,
                ownerId = 1,
                name = "Whiskers",
                species = PetSpecies.CAT,
                breed = "Persian",
                dateOfBirth = System.currentTimeMillis(),
                gender = PetGender.FEMALE,
                reproductiveStatus = ReproductiveStatus.SPAYED,
                weight = 4.5,
                color = "White"
            )
        )
    }

    LaunchedEffect(selectedDate, selectedVeterinarian, appointmentType) {
        selectedVeterinarian?.let { vetId ->
            viewModel.loadAvailableSlotsForDate(selectedDate, vetId, appointmentType)
        }
    }

    LaunchedEffect(uiState.createdAppointmentId) {
        if (uiState.createdAppointmentId != null) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Book Appointment", fontSize = 20.sp, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            item {
                BookingProgressCard(currentStep = currentStep)
            }

            when (currentStep) {
                BookingStep.SELECT_PET -> {
                    item {
                        SelectPetCard(
                            pets = pets,
                            selectedPet = selectedPet,
                            onPetSelected = { pet ->
                                selectedPet = pet
                                currentStep = BookingStep.SELECT_APPOINTMENT_TYPE
                            }
                        )
                    }
                }
                
                BookingStep.SELECT_APPOINTMENT_TYPE -> {
                    item {
                        SelectAppointmentTypeCard(
                            selectedType = appointmentType,
                            selectedUrgency = urgencyLevel,
                            onTypeSelected = { appointmentType = it },
                            onUrgencySelected = { urgencyLevel = it },
                            onNext = { currentStep = BookingStep.SELECT_VETERINARIAN }
                        )
                    }
                }
                
                BookingStep.SELECT_VETERINARIAN -> {
                    item {
                        SelectVeterinarianCard(
                            selectedVetId = selectedVeterinarian,
                            onVetSelected = { vetId ->
                                selectedVeterinarian = vetId
                                currentStep = BookingStep.SELECT_DATE_TIME
                            }
                        )
                    }
                }
                
                BookingStep.SELECT_DATE_TIME -> {
                    item {
                        SelectDateTimeCard(
                            selectedDate = selectedDate,
                            selectedSlot = selectedSlot,
                            availableSlots = uiState.availableSlots,
                            isLoadingSlots = uiState.isLoadingSlots,
                            onDateSelected = { date -> viewModel.selectDate(date) },
                            onSlotSelected = { slot ->
                                selectedSlot = slot
                                currentStep = BookingStep.APPOINTMENT_DETAILS
                            }
                        )
                    }
                }
                
                BookingStep.APPOINTMENT_DETAILS -> {
                    item {
                        AppointmentDetailsCard(
                            reason = reason,
                            onReasonChanged = { reason = it },
                            onNext = { currentStep = BookingStep.REVIEW_AND_CONFIRM }
                        )
                    }
                }
                
                BookingStep.REVIEW_AND_CONFIRM -> {
                    item {
                        ReviewAndConfirmCard(
                            pet = selectedPet!!,
                            appointmentType = appointmentType,
                            urgencyLevel = urgencyLevel,
                            selectedDate = selectedDate,
                            selectedSlot = selectedSlot!!,
                            reason = reason,
                            isCreating = uiState.isCreatingAppointment,
                            onConfirm = {
                                viewModel.createAppointment(
                                    petId = selectedPet!!.id,
                                    ownerId = selectedPet!!.ownerId,
                                    veterinarianId = selectedVeterinarian!!,
                                    appointmentType = appointmentType,
                                    date = selectedDate,
                                    startTime = selectedSlot!!.startTime,
                                    endTime = selectedSlot!!.endTime,
                                    reason = reason,
                                    urgencyLevel = urgencyLevel
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class BookingStep {
    SELECT_PET,
    SELECT_APPOINTMENT_TYPE,
    SELECT_VETERINARIAN,
    SELECT_DATE_TIME,
    APPOINTMENT_DETAILS,
    REVIEW_AND_CONFIRM
}

@Composable
private fun BookingProgressCard(currentStep: BookingStep) {
    val steps = BookingStep.values()
    val currentIndex = steps.indexOf(currentStep)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Step ${currentIndex + 1} of ${steps.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = when (currentStep) {
                    BookingStep.SELECT_PET -> "Select Your Pet"
                    BookingStep.SELECT_APPOINTMENT_TYPE -> "Appointment Type"
                    BookingStep.SELECT_VETERINARIAN -> "Choose Veterinarian"
                    BookingStep.SELECT_DATE_TIME -> "Select Date & Time"
                    BookingStep.APPOINTMENT_DETAILS -> "Appointment Details"
                    BookingStep.REVIEW_AND_CONFIRM -> "Review & Confirm"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / steps.size,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectPetCard(
    pets: List<Pet>,
    selectedPet: Pet?,
    onPetSelected: (Pet) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Your Pet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (pets.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text("No pets registered", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                pets.forEach { pet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPet?.id == pet.id,
                                onClick = { onPetSelected(pet) },
                                role = Role.RadioButton
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedPet?.id == pet.id) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPet?.id == pet.id,
                                onClick = { onPetSelected(pet) }
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${pet.breed} • ${pet.species}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SelectAppointmentTypeCard(
    selectedType: AppointmentType,
    selectedUrgency: UrgencyLevel,
    onTypeSelected: (AppointmentType) -> Unit,
    onUrgencySelected: (UrgencyLevel) -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Appointment Type",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Type of Appointment", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            AppointmentType.values().take(5).forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = type.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text("Urgency Level", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            UrgencyLevel.values().forEach { urgency ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedUrgency == urgency,
                            onClick = { onUrgencySelected(urgency) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedUrgency == urgency,
                        onClick = { onUrgencySelected(urgency) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = urgency.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FilledTonalButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun SelectVeterinarianCard(
    selectedVetId: Long?,
    onVetSelected: (Long) -> Unit
) {
    val veterinarians = remember {
        listOf(
            Triple(1L, "Dr. Sarah Johnson", "Small Animal Specialist"),
            Triple(2L, "Dr. Michael Chen", "Emergency Veterinarian")
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Choose Veterinarian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            veterinarians.forEach { (id, name, specialty) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedVetId == id,
                            onClick = { onVetSelected(id) },
                            role = Role.RadioButton
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedVetId == id) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVetId == id,
                            onClick = { onVetSelected(id) }
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = specialty,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SelectDateTimeCard(
    selectedDate: LocalDate,
    selectedSlot: AvailableTimeSlot?,
    availableSlots: List<AvailableTimeSlot>,
    isLoadingSlots: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onSlotSelected: (AvailableTimeSlot) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Date & Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            CalendarView(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                availableSlots = availableSlots,
                onSlotSelected = onSlotSelected,
                viewMode = CalendarViewMode.DAY
            )
            
            if (isLoadingSlots) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AppointmentDetailsCard(
    reason: String,
    onReasonChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Appointment Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reason,
                onValueChange = onReasonChanged,
                label = { Text("Reason for Visit") },
                placeholder = { Text("e.g., Annual checkup, vaccination...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FilledTonalButton(
                onClick = onNext,
                enabled = reason.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review Appointment")
            }
        }
    }
}

@Composable
private fun ReviewAndConfirmCard(
    pet: Pet,
    appointmentType: AppointmentType,
    urgencyLevel: UrgencyLevel,
    selectedDate: LocalDate,
    selectedSlot: AvailableTimeSlot,
    reason: String,
    isCreating: Boolean,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Review & Confirm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Pet Details
            Text("Pet", style = MaterialTheme.typography.labelMedium, 
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${pet.name} (${pet.breed})", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Appointment Type
            Text("Appointment Type", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${appointmentType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} • ${urgencyLevel.name.lowercase().replaceFirstChar { it.uppercase() }} Priority", 
                 style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Date & Time
            Text("Date & Time", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))}\n${selectedSlot.startTime} - ${selectedSlot.endTime} with ${selectedSlot.veterinarianName}",
                 style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Details
            Text("Details", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(reason, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FilledTonalButton(
                onClick = onConfirm,
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Confirm Appointment")
                }
            }
        }
    }
}
