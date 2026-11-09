package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ExpenseRepository
import com.example.ui.ExpenseViewModel
import com.example.ui.MainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.theme.SpendWiseTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("spendwise_prefs", Context.MODE_PRIVATE) }
    var isGuestMode by remember { mutableStateOf(prefs.getBoolean("is_guest_mode", false)) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isGuestMode = false
                prefs.edit().putBoolean("is_guest_mode", false).apply()
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null && !isGuestMode) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                isGuestMode = false
                prefs.edit().putBoolean("is_guest_mode", false).apply()
            },
            onContinueAsGuest = {
                isGuestMode = true
                prefs.edit().putBoolean("is_guest_mode", true).apply()
            }
        )
    } else {
        val isGuest = currentUser == null
        val repository = remember(currentUser?.uid, isGuest) { ExpenseRepository(context, isGuest = isGuest) }
        val viewModel: ExpenseViewModel = viewModel(
            key = if (isGuest) "guest_session" else currentUser?.uid,
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExpenseViewModel(repository) as T
                }
            }
        )
        MainScreen(
            viewModel = viewModel,
            isGuest = isGuest,
            onSignOut = {
                currentUser = null
                isGuestMode = false
                prefs.edit().putBoolean("is_guest_mode", false).apply()
            },
            onSignInClick = {
                isGuestMode = false
                prefs.edit().putBoolean("is_guest_mode", false).apply()
            }
        )
    }
}
