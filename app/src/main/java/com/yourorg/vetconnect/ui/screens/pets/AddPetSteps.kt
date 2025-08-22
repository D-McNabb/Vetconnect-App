package com.yourorg.vetconnect.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.ui.components.*
import com.yourorg.vetconnect.viewmodel.PetViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BasicInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    species: PetSpecies?,
    onSpeciesChange: (PetSpecies) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    dateOfBirth: LocalDate?,
    onDateOfBirthChange: (LocalDate) -> Unit,
    gender: PetGender?,
    onGenderChange: (PetGender) -> Unit,
    reproductiveStatus: ReproductiveStatus?,
    onReproductiveStatusChange: (ReproductiveStatus) -> Unit,
    validation: FormValidation
) {
    ClinicalSection(
        title = "Tell us about your pet",
        subtitle = "This information helps us provide the best care"
    ) {
        ClinicalTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Pet Name",
            placeholder = "What's your pet's name?",
            isRequired = true,
            isError = validation.errors.containsKey("name"),
            errorMessage = validation.errors["name"],
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ClinicalDropdown(
            value = species,
            onValueChange = onSpeciesChange,
            options = PetSpecies.values().toList(),
            label = "Species",
            optionLabel = { it.name.replace("_", " ") },
            isRequired = true,
            isError = validation.errors.containsKey("species"),
            errorMessage = validation.errors["species"],
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ClinicalTextField(
            value = breed,
            onValueChange = onBreedChange,
            label = "Breed",
            placeholder = "Enter breed (e.g., Golden Retriever, Persian)",
            isRequired = true,
            isError = validation.errors.containsKey("breed"),
            errorMessage = validation.errors["breed"],
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ClinicalDatePicker(
            selectedDate = dateOfBirth,
            onDateSelected = onDateOfBirthChange,
            label = "Date of Birth",
            isRequired = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClinicalDropdown(
                value = gender,
                onValueChange = onGenderChange,
                options = PetGender.values().toList(),
                label = "Gender",
                optionLabel = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } },
                isRequired = true,
                isError = validation.errors.containsKey("gender"),
                errorMessage = validation.errors["gender"],
                modifier = Modifier.weight(1f)
            )
            
            ClinicalDropdown(
                value = reproductiveStatus,
                onValueChange = onReproductiveStatusChange,
                options = ReproductiveStatus.values().toList(),
                label = "Spay/Neuter Status",
                optionLabel = { 
                    when (it) {
                        ReproductiveStatus.INTACT -> "Intact"
                        ReproductiveStatus.NEUTERED -> "Neutered"
                        ReproductiveStatus.SPAYED -> "Spayed"
                        ReproductiveStatus.UNKNOWN -> "Unknown"
                    }
                },
                isRequired = true,
                isError = validation.errors.containsKey("reproductiveStatus"),
                errorMessage = validation.errors["reproductiveStatus"],
                modifier = Modifier.weight(1f)
            )
        }
        
        // Helpful tips
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                )
                Column {
                    Text(
                        text = "💡 Helpful Tips",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Double-check the spelling of your pet's name\n" +
                               "• If you're unsure about the exact breed, use the closest match\n" +
                               "• For mixed breeds, list the primary breed or use 'Mixed'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PhysicalDetailsStep(
    weight: Double?,
    onWeightChange: (Double?) -> Unit,
    height: Double?,
    onHeightChange: (Double?) -> Unit,
    length: Double?,
    onLengthChange: (Double?) -> Unit,
    color: String,
    onColorChange: (String) -> Unit,
    markings: String,
    onMarkingsChange: (String) -> Unit,
    distinguishingFeatures: String,
    onDistinguishingFeaturesChange: (String) -> Unit,
    validation: FormValidation
) {
    ClinicalSection(
        title = "Physical Characteristics",
        subtitle = "Help us identify your pet and track their health"
    ) {
        Text(
            text = "Measurements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeightInput(
                weight = weight,
                onWeightChange = onWeightChange,
                isRequired = true,
                modifier = Modifier.weight(1f)
            )
            
            WeightInput(
                weight = height,
                onWeightChange = onHeightChange,
                unit = "cm",
                label = "Height",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WeightInput(
            weight = length,
            onWeightChange = onLengthChange,
            unit = "cm",
            label = "Length (nose to tail)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        ClinicalTextField(
            value = color,
            onValueChange = onColorChange,
            label = "Primary Color",
            placeholder = "e.g., Golden, Black, White, Tabby",
            isRequired = true,
            isError = validation.errors.containsKey("color"),
            errorMessage = validation.errors["color"],
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ClinicalTextField(
            value = markings,
            onValueChange = onMarkingsChange,
            label = "Markings",
            placeholder = "e.g., White chest, Black spots, Striped",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ClinicalTextField(
            value = distinguishingFeatures,
            onValueChange = onDistinguishingFeaturesChange,
            label = "Distinguishing Features",
            placeholder = "e.g., Scar on left ear, Unique eye color, Missing tooth",
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Photo placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Add Pet Photo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Coming soon - Photo upload feature",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun IdentificationStep(
    microchipNumber: String,
    onMicrochipNumberChange: (String) -> Unit,
    registrationNumber: String,
    onRegistrationNumberChange: (String) -> Unit,
    tattoNumber: String,
    onTattoNumberChange: (String) -> Unit
) {
    ClinicalSection(
        title = "Identification Information",
        subtitle = "Help ensure your pet can be safely returned if lost"
    ) {
        ClinicalTextField(
            value = microchipNumber,
            onValueChange = onMicrochipNumberChange,
            label = "Microchip Number",
            placeholder = "15-digit microchip number",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClinicalTextField(
                value = registrationNumber,
                onValueChange = onRegistrationNumberChange,
                label = "Registration Number",
                placeholder = "Breed registry number",
                modifier = Modifier.weight(1f)
            )
            
            ClinicalTextField(
                value = tattoNumber,
                onValueChange = onTattoNumberChange,
                label = "Tattoo Number",
                placeholder = "ID tattoo",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Information card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Why This Matters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Microchips are the most reliable way to identify your pet if they get lost. Registration numbers help with breeding records and pedigree verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MedicalInfoStep(
    bloodType: String,
    onBloodTypeChange: (String) -> Unit,
    allergies: List<String>,
    onAllergiesChange: (List<String>) -> Unit,
    chronicConditions: List<String>,
    onChronicConditionsChange: (List<String>) -> Unit,
    currentMedications: List<String>,
    onCurrentMedicationsChange: (List<String>) -> Unit,
    dietaryRestrictions: List<String>,
    onDietaryRestrictionsChange: (List<String>) -> Unit,
    behavioralNotes: String,
    onBehavioralNotesChange: (String) -> Unit
) {
    ClinicalSection(
        title = "Medical Information",
        subtitle = "Important health details for veterinary care"
    ) {
        ClinicalTextField(
            value = bloodType,
            onValueChange = onBloodTypeChange,
            label = "Blood Type",
            placeholder = "If known (e.g., DEA 1.1+)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        DynamicList(
            items = allergies,
            onItemsChange = onAllergiesChange,
            label = "Known Allergies",
            placeholder = "Enter allergy (e.g., Chicken, Pollen)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        DynamicList(
            items = chronicConditions,
            onItemsChange = onChronicConditionsChange,
            label = "Chronic Conditions",
            placeholder = "Enter condition (e.g., Arthritis, Diabetes)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        DynamicList(
            items = currentMedications,
            onItemsChange = onCurrentMedicationsChange,
            label = "Current Medications",
            placeholder = "Enter medication and dosage",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        DynamicList(
            items = dietaryRestrictions,
            onItemsChange = onDietaryRestrictionsChange,
            label = "Dietary Restrictions",
            placeholder = "Enter restriction (e.g., Grain-free)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ClinicalTextField(
            value = behavioralNotes,
            onValueChange = onBehavioralNotesChange,
            label = "Behavioral Notes",
            placeholder = "Temperament, fears, special handling requirements...",
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EmergencyContactsStep(
    emergencyContactName: String,
    onEmergencyContactNameChange: (String) -> Unit,
    emergencyContactPhone: String,
    onEmergencyContactPhoneChange: (String) -> Unit,
    insuranceProvider: String,
    onInsuranceProviderChange: (String) -> Unit,
    insurancePolicyNumber: String,
    onInsurancePolicyNumberChange: (String) -> Unit,
    validation: FormValidation
) {
    ClinicalSection(
        title = "Emergency Contacts & Insurance",
        subtitle = "Important information for emergency situations"
    ) {
        Text(
            text = "Emergency Contact",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClinicalTextField(
                value = emergencyContactName,
                onValueChange = onEmergencyContactNameChange,
                label = "Contact Name",
                placeholder = "Full name",
                isError = validation.errors.containsKey("emergencyContactName"),
                errorMessage = validation.errors["emergencyContactName"],
                modifier = Modifier.weight(1f)
            )
            
            ClinicalTextField(
                value = emergencyContactPhone,
                onValueChange = onEmergencyContactPhoneChange,
                label = "Phone Number",
                placeholder = "+1 (555) 123-4567",
                keyboardType = KeyboardType.Phone,
                isError = validation.errors.containsKey("emergencyContactPhone"),
                errorMessage = validation.errors["emergencyContactPhone"],
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Pet Insurance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ClinicalTextField(
                value = insuranceProvider,
                onValueChange = onInsuranceProviderChange,
                label = "Insurance Provider",
                placeholder = "e.g., Petplan, ASPCA",
                modifier = Modifier.weight(1f)
            )
            
            ClinicalTextField(
                value = insurancePolicyNumber,
                onValueChange = onInsurancePolicyNumberChange,
                label = "Policy Number",
                placeholder = "Policy ID",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Information card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Emergency Preparedness",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Having an emergency contact helps us reach someone when you're unavailable. Pet insurance can help manage unexpected veterinary costs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewStep(
    name: String,
    species: PetSpecies?,
    breed: String,
    dateOfBirth: LocalDate?,
    gender: PetGender?,
    reproductiveStatus: ReproductiveStatus?,
    weight: Double?,
    color: String,
    allergies: List<String>,
    chronicConditions: List<String>,
    emergencyContactName: String,
    viewModel: PetViewModel
) {
    ClinicalSection(
        title = "Review Pet Information",
        subtitle = "Please review all details before saving"
    ) {
        ReviewCard(title = "Basic Information") {
            ReviewItem("Name", name)
            ReviewItem("Species", species?.name?.replace("_", " ") ?: "")
            ReviewItem("Breed", breed)
            ReviewItem("Age", dateOfBirth?.let { viewModel.calculateAge(it.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) } ?: "")
            ReviewItem("Gender", gender?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
            ReviewItem("Spay/Neuter", reproductiveStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ReviewCard(title = "Physical Details") {
            ReviewItem("Weight", weight?.let { "$it kg" } ?: "")
            ReviewItem("Color", color)
        }
        
        if (allergies.isNotEmpty() || chronicConditions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            ReviewCard(title = "Medical Information") {
                if (allergies.isNotEmpty()) {
                    ReviewItem("Allergies", allergies.joinToString(", "))
                }
                if (chronicConditions.isNotEmpty()) {
                    ReviewItem("Chronic Conditions", chronicConditions.joinToString(", "))
                }
            }
        }
        
        if (emergencyContactName.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            ReviewCard(title = "Emergency Contact") {
                ReviewItem("Contact Name", emergencyContactName)
            }
        }
        
        // Save confirmation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                Column {
                    Text(
                        text = "Ready to add $name to your family!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All information looks good. Click 'Save Pet' to complete registration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun ReviewItem(
    label: String,
    value: String
) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
