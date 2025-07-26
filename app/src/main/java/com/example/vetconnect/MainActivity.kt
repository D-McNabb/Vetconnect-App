package com.example.vetconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.vetconnect.ui.navigation.Screen
import com.example.vetconnect.ui.navigation.VetConnectNavGraph
import com.example.vetconnect.ui.theme.VetConnectTheme
import com.example.vetconnect.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VetConnectApp()
        }
    }
}

@Composable
fun VetConnectApp(
    authViewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    
    VetConnectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val startDestination = when (authState) {
                is com.example.vetconnect.ui.state.AuthState.Authenticated -> Screen.Dashboard.route
                is com.example.vetconnect.ui.state.AuthState.Unauthenticated -> Screen.Login.route
                is com.example.vetconnect.ui.state.AuthState.Loading -> Screen.Login.route
                is com.example.vetconnect.ui.state.AuthState.Error -> Screen.Login.route
            }
            
            VetConnectNavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}