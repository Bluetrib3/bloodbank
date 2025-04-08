package com.example.bloodbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bloodbank.ui.theme.BLOODBANKTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val user = FirebaseAuth.getInstance().currentUser
        val startDestination = if (user != null) "dashboard" else "landing"

        setContent {
            BLOODBANKTheme {
                AppNavigation()
            }
        }
    }
}
