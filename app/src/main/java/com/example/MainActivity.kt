package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.util.Log
import com.google.firebase.FirebaseApp
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CryptoViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val initialized = FirebaseApp.getApps(this).isNotEmpty()
            Log.d("MainActivity", "Firebase GMS services enabled: $initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization check failed", e)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    var authScreen by remember { mutableStateOf("login") }

    if (currentUserId == null) {
        when (authScreen) {
            "login" -> {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignUp = { authScreen = "signup" },
                    modifier = modifier
                )
            }
            "signup" -> {
                SignUpScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { authScreen = "login" },
                    modifier = modifier
                )
            }
        }
    } else {
        if (isAdmin) {
            AdminDashboardScreen(
                viewModel = viewModel,
                modifier = modifier
            )
        } else {
            MainDashboardScreen(
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}

