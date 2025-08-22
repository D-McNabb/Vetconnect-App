package com.yourorg.vetconnect.ui.screens.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourorg.vetconnect.model.*
import com.yourorg.vetconnect.navigation.Screen
import com.yourorg.vetconnect.viewmodel.PetViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(
        factory = PetViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedSpeciesFilter by remember { mutableStateOf<PetSpecies?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Pets", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Filter")
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddPet.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Pet")
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isNotBlank()) {
                        viewModel.searchPets(it)
                    } else {
                        viewModel.clearSearchResults()
                    }
                },
                label = { Text("Search pets...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Filter Chips
            if (selectedSpeciesFilter != null || uiState.searchResults.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedSpeciesFilter != null) {
                        FilterChip(
                            onClick = { 
                                selectedSpeciesFilter = null
                                viewModel.clearSearchResults()
                            },
                            label = { Text(selectedSpeciesFilter!!.name) },
                            selected = true
                        )
                    }
                    if (uiState.searchResults.isNotEmpty()) {
                        FilterChip(
                            onClick = { 
                                searchQuery = ""
                                viewModel.clearSearchResults()
                            },
                            label = { Text("Search Results") },
                            selected = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Pet List
            when {
                uiState.isLoading -> {
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
                                onClick = { viewModel.loadPets() },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {
                    val petsToShow = when {
                        uiState.searchResults.isNotEmpty() -> uiState.searchResults
                        uiState.filteredPets.isNotEmpty() -> uiState.filteredPets
                        else -> uiState.pets
                    }
                    
                    if (petsToShow.isEmpty()) {
                        EmptyPetsState(
                            onAddPetClick = { navController.navigate(Screen.AddPet.route) }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(petsToShow) { pet ->
                                PetListItem(
                                    pet = pet,
                                    onPetClick = { 
                                        navController.navigate(Screen.PetDetail.createRoute(pet.id))
                                    },
                                    calculateAge = { dateOfBirth ->
                                        viewModel.calculateAge(dateOfBirth)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Pets") },
            text = {
                Column {
                    Text("Filter by Species:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    PetSpecies.values().forEach { species ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSpeciesFilter = species
                                    viewModel.filterPetsBySpecies(species)
                                    showFilterDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSpeciesFilter == species,
                                onClick = null
                            )
                            Text(
                                text = species.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedSpeciesFilter = null
                        viewModel.clearSearchResults()
                        showFilterDialog = false
                    }
                ) {
                    Text("Clear")
                }
            }
        )
    }
}

@Composable
fun PetListItem(
    pet: Pet,
    onPetClick: () -> Unit,
    calculateAge: (Long) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPetClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Pet",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Pet Information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${pet.species.name.replace("_", " ")} • ${pet.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row {
                    Text(
                        text = "Age: ${calculateAge(pet.dateOfBirth)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = " • Weight: ${pet.weight} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Health indicators
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pet.allergies.isNotEmpty()) {
                        HealthIndicatorChip(
                            text = "Allergies",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (pet.chronicConditions.isNotEmpty()) {
                        HealthIndicatorChip(
                            text = "Chronic",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (pet.currentMedications.isNotEmpty()) {
                        HealthIndicatorChip(
                            text = "Medications",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthIndicatorChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyPetsState(
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "No pets",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No pets yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Add your first pet to get started with their medical records",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddPetClick,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Pet")
            }
        }
    }
}
