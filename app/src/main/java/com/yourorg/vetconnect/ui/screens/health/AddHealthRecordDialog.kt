package com.yourorg.vetconnect.ui.screens.health

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.ui.components.*
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthRecordDialog(
    petId: Long,
    preselectedType: HealthRecordType? = null,
    onDismiss: () -> Unit,
    onSave: (HealthRecord) -> Unit
) {
    var recordType by remember { mutableStateOf(preselectedType ?: HealthRecordType.CHECKUP) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var recordDate by remember { mutableStateOf(LocalDate.now()) }
    var nextDueDate by remember { mutableStateOf<LocalDate?>(null) }
    var hasNextDueDate by remember { mutableStateOf(false) }
    var veterinarianName by remember { mutableStateOf("") }
    
    // Validation
    val isValid = title.isNotBlank()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Health Record",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Record Type Selection
                Text(
                    text = "Record Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                RecordTypeSelector(
                    selectedType = recordType,
                    onTypeSelected = { recordType = it },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title
                ClinicalTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    placeholder = getRecordTitlePlaceholder(recordType),
                    isRequired = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                ClinicalTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description/Notes",
                    placeholder = "Enter detailed notes about this record...",
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Record Date
                ClinicalDatePicker(
                    selectedDate = recordDate,
                    onDateSelected = { recordDate = it },
                    label = "Record Date",
                    isRequired = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Veterinarian
                ClinicalTextField(
                    value = veterinarianName,
                    onValueChange = { veterinarianName = it },
                    label = "Veterinarian",
                    placeholder = "Dr. Smith",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Next Due Date (for certain record types)
                if (recordType in listOf(HealthRecordType.VACCINATION, HealthRecordType.MEDICATION, HealthRecordType.CHECKUP)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasNextDueDate,
                            onCheckedChange = { 
                                hasNextDueDate = it
                                if (!it) nextDueDate = null
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (recordType) {
                                HealthRecordType.VACCINATION -> "Schedule next vaccination"
                                HealthRecordType.MEDICATION -> "Set medication reminder"
                                HealthRecordType.CHECKUP -> "Schedule follow-up"
                                else -> "Set reminder date"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = hasNextDueDate,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            ClinicalDatePicker(
                                selectedDate = nextDueDate,
                                onDateSelected = { nextDueDate = it },
                                label = when (recordType) {
                                    HealthRecordType.VACCINATION -> "Next Vaccination Due"
                                    HealthRecordType.MEDICATION -> "Next Dose Due"
                                    HealthRecordType.CHECKUP -> "Follow-up Date"
                                    else -> "Next Due Date"
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val healthRecord = HealthRecord(
                                petId = petId,
                                recordType = recordType,
                                title = title.trim(),
                                description = description.trim(),
                                date = recordDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                nextDueDate = nextDueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                                veterinarianId = null // TODO: Handle veterinarian selection
                            )
                            onSave(healthRecord)
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}

@Composable
fun RecordTypeSelector(
    selectedType: HealthRecordType,
    onTypeSelected: (HealthRecordType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val recordTypes = listOf(
            HealthRecordType.VACCINATION to Pair("Vaccination", Icons.Default.Add),
            HealthRecordType.CHECKUP to Pair("Check-up", Icons.Default.Person),
            HealthRecordType.TREATMENT to Pair("Treatment", Icons.Default.Star),
            HealthRecordType.SURGERY to Pair("Surgery", Icons.Default.Edit),
            HealthRecordType.MEDICATION to Pair("Medication", Icons.Default.Add),
            HealthRecordType.ALLERGY to Pair("Allergy", Icons.Default.Warning),
            HealthRecordType.OTHER to Pair("Other", Icons.Default.Star)
        )
        
        recordTypes.chunked(2).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEach { (type, typeInfo) ->
                    val (label, icon) = typeInfo
                    val isSelected = selectedType == type
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            )
                        } else null,
                        onClick = { onTypeSelected(type) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Add empty space if odd number of items in row
                if (rowTypes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            if (recordTypes.indexOf(rowTypes.first()) < recordTypes.size - 2) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun getRecordTitlePlaceholder(recordType: HealthRecordType): String {
    return when (recordType) {
        HealthRecordType.VACCINATION -> "e.g., Rabies Vaccination"
        HealthRecordType.CHECKUP -> "e.g., Annual Health Check"
        HealthRecordType.TREATMENT -> "e.g., Flea Treatment"
        HealthRecordType.SURGERY -> "e.g., Spay Surgery"
        HealthRecordType.MEDICATION -> "e.g., Antibiotics Course"
        HealthRecordType.ALLERGY -> "e.g., Food Allergy Reaction"
        HealthRecordType.OTHER -> "Enter record title"
    }
}
