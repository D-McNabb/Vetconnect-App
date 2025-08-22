package com.yourorg.vetconnect.ui.screens.health

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.viewmodel.HealthRecordsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

enum class HealthRecordFilter {
    ALL, VACCINATIONS, TREATMENTS, CHECKUPS, SURGERIES, MEDICATIONS
}

data class HealthSummaryItem(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val color: Color,
    val action: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ImprovedHealthRecordsScreen(
    navController: NavController,
    petId: Long
) {
    val context = LocalContext.current
    val viewModel: HealthRecordsViewModel = viewModel(
        factory = HealthRecordsViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val healthRecords by viewModel.healthRecords.collectAsState()
    val vaccinations by viewModel.vaccinations.collectAsState()
    val weightRecords by viewModel.weightRecords.collectAsState()
    
    var selectedFilter by remember { mutableStateOf(HealthRecordFilter.ALL) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var selectedRecordType by remember { mutableStateOf<HealthRecordType?>(null) }
    
    LaunchedEffect(petId) {
        viewModel.loadHealthData(petId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Health Records",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.petName ?: "Loading...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddRecordDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Record")
                    }
                    IconButton(
                        onClick = { /* Navigate to health insights */ }
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Health Insights")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRecordDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Health Record")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Summary Dashboard
            item {
                HealthSummaryDashboard(
                    healthRecords = healthRecords,
                    vaccinations = vaccinations,
                    weightRecords = weightRecords,
                    onSummaryClick = { /* Navigate to specific view */ }
                )
            }
            
            // Filter Chips
            item {
                HealthRecordFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    recordCounts = viewModel.getRecordCounts(healthRecords)
                )
            }
            
            // Weight Chart (if weight records exist)
            if (weightRecords.isNotEmpty()) {
                item {
                    WeightProgressChart(
                        weightRecords = weightRecords,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Upcoming/Overdue Items
            item {
                UpcomingHealthItemsCard(
                    upcomingVaccinations = viewModel.getUpcomingVaccinations(vaccinations),
                    overdueItems = viewModel.getOverdueItems(healthRecords)
                )
            }
            
            // Health Records Timeline
            item {
                Text(
                    text = "Medical Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Filtered Records
            val filteredRecords = when (selectedFilter) {
                HealthRecordFilter.ALL -> healthRecords
                HealthRecordFilter.VACCINATIONS -> healthRecords.filter { it.recordType == HealthRecordType.VACCINATION }
                HealthRecordFilter.TREATMENTS -> healthRecords.filter { it.recordType == HealthRecordType.TREATMENT }
                HealthRecordFilter.CHECKUPS -> healthRecords.filter { it.recordType == HealthRecordType.CHECKUP }
                HealthRecordFilter.SURGERIES -> healthRecords.filter { it.recordType == HealthRecordType.SURGERY }
                HealthRecordFilter.MEDICATIONS -> healthRecords.filter { it.recordType == HealthRecordType.MEDICATION }
            }
            
            if (filteredRecords.isEmpty()) {
                item {
                    EmptyHealthRecordsCard(
                        selectedFilter = selectedFilter,
                        onAddRecord = { 
                            selectedRecordType = when (selectedFilter) {
                                HealthRecordFilter.VACCINATIONS -> HealthRecordType.VACCINATION
                                HealthRecordFilter.TREATMENTS -> HealthRecordType.TREATMENT
                                HealthRecordFilter.CHECKUPS -> HealthRecordType.CHECKUP
                                HealthRecordFilter.SURGERIES -> HealthRecordType.SURGERY
                                HealthRecordFilter.MEDICATIONS -> HealthRecordType.MEDICATION
                                else -> null
                            }
                            showAddRecordDialog = true
                        }
                    )
                }
            } else {
                items(filteredRecords) { record ->
                    HealthRecordTimelineItem(
                        record = record,
                        onClick = { /* Navigate to record detail */ },
                        onEdit = { /* Navigate to edit */ },
                        onDelete = { viewModel.deleteHealthRecord(record.id) }
                    )
                }
            }
        }
    }
    
    // Add Record Dialog
    if (showAddRecordDialog) {
        AddHealthRecordDialog(
            petId = petId,
            preselectedType = selectedRecordType,
            onDismiss = { 
                showAddRecordDialog = false
                selectedRecordType = null
            },
            onSave = { record ->
                viewModel.addHealthRecord(record)
                showAddRecordDialog = false
                selectedRecordType = null
            }
        )
    }
}

@Composable
fun HealthSummaryDashboard(
    healthRecords: List<HealthRecord>,
    vaccinations: List<Vaccination>,
    weightRecords: List<WeightRecord>,
    onSummaryClick: (String) -> Unit
) {
    val recentRecords = healthRecords.filter { 
        val recordDate = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        val daysSince = java.time.temporal.ChronoUnit.DAYS.between(recordDate, LocalDate.now())
        daysSince <= 30
    }
    
    val upcomingVaccinations = vaccinations.count { 
        it.nextDueDate?.let {
            val dueDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
            daysUntil in 0..30
        } ?: false
    }
    
    val currentWeight = weightRecords.maxByOrNull { it.recordDate }?.weight
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthSummaryCard(
                    title = "Recent Records",
                    value = "${recentRecords.size}",
                    subtitle = "Last 30 days",
                    icon = Icons.Default.Info,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onSummaryClick("recent") }
                )
                
                HealthSummaryCard(
                    title = "Due Soon",
                    value = "$upcomingVaccinations",
                    subtitle = "Vaccinations",
                    icon = Icons.Default.Star,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { onSummaryClick("upcoming") }
                )
                
                currentWeight?.let { weight ->
                    HealthSummaryCard(
                        title = "Current Weight",
                        value = "${weight.roundToInt()}",
                        subtitle = "kg",
                        icon = Icons.Default.Edit,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSummaryClick("weight") }
                    )
                }
            }
        }
    }
}

@Composable
fun HealthSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HealthRecordFilterChips(
    selectedFilter: HealthRecordFilter,
    onFilterSelected: (HealthRecordFilter) -> Unit,
    recordCounts: Map<HealthRecordFilter, Int>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(HealthRecordFilter.values()) { filter ->
            val isSelected = selectedFilter == filter
            val count = recordCounts[filter] ?: 0
            
            FilterChip(
                onClick = { onFilterSelected(filter) },
                label = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filter.name.lowercase().replaceFirstChar { it.uppercase() }
                        )
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($count)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                selected = isSelected,
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun WeightProgressChart(
    weightRecords: List<WeightRecord>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Simple weight progression visualization
            val sortedRecords = weightRecords.sortedBy { it.recordDate }
            if (sortedRecords.size >= 2) {
                val latest = sortedRecords.last()
                val previous = sortedRecords[sortedRecords.size - 2]
                val weightChange = latest.weight - previous.weight
                val isIncreasing = weightChange > 0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current: ${latest.weight} kg",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Previous: ${previous.weight} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isIncreasing) Icons.Default.Star else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isIncreasing) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (isIncreasing) "+" else ""}${String.format("%.1f", weightChange)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isIncreasing) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Text(
                    text = "Add more weight records to see progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpcomingHealthItemsCard(
    upcomingVaccinations: List<Vaccination>,
    overdueItems: List<HealthRecord>
) {
    if (upcomingVaccinations.isEmpty() && overdueItems.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (overdueItems.isNotEmpty()) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    if (overdueItems.isNotEmpty()) Icons.Default.Warning else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (overdueItems.isNotEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (overdueItems.isNotEmpty()) "Action Required" else "Upcoming Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Overdue items first
            overdueItems.forEach { item ->
                UpcomingItemRow(
                    title = item.title,
                    subtitle = "Overdue since ${formatDate(item.nextDueDate ?: item.date)}",
                    isOverdue = true
                )
            }
            
            // Upcoming vaccinations
            upcomingVaccinations.forEach { vaccination ->
                vaccination.nextDueDate?.let { dueDate ->
                    UpcomingItemRow(
                        title = vaccination.vaccineName,
                        subtitle = "Due ${formatDate(dueDate)}",
                        isOverdue = false
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingItemRow(
    title: String,
    subtitle: String,
    isOverdue: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isOverdue) Icons.Default.Warning else Icons.Default.Star,
            contentDescription = null,
            tint = if (isOverdue) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthRecordTimelineItem(
    record: HealthRecord,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Record type indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                getRecordTypeColor(record.recordType),
                                CircleShape
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = record.recordType.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = getRecordTypeColor(record.recordType)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatDate(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
            
            if (record.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Next due date if applicable
            record.nextDueDate?.let { dueDate ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Next due: ${formatDate(dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHealthRecordsCard(
    selectedFilter: HealthRecordFilter,
    onAddRecord: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (selectedFilter) {
                    HealthRecordFilter.ALL -> "No health records yet"
                    else -> "No ${selectedFilter.name.lowercase()} records"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add your first health record to start tracking your pet's medical history",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Button(
                onClick = onAddRecord,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Record")
            }
        }
    }
}

@Composable
fun getRecordTypeColor(recordType: HealthRecordType): Color {
    return when (recordType) {
        HealthRecordType.VACCINATION -> MaterialTheme.colorScheme.primary
        HealthRecordType.TREATMENT -> MaterialTheme.colorScheme.secondary
        HealthRecordType.CHECKUP -> MaterialTheme.colorScheme.tertiary
        HealthRecordType.SURGERY -> MaterialTheme.colorScheme.error
        HealthRecordType.MEDICATION -> MaterialTheme.colorScheme.outline
        HealthRecordType.ALLERGY -> MaterialTheme.colorScheme.error
        HealthRecordType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

fun formatDate(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
