package com.example.bloodbank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "No email found"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("landing") {
                            popUpTo("landing") { inclusive = true } // Clears all previous screens
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        ProfileContent(userEmail, Modifier.padding(paddingValues), navController)
    }
}

@Composable
fun ProfileContent(email: String, modifier: Modifier = Modifier, navController: NavController) {
    // Calculate joined date
    val creationTimeMillis = FirebaseAuth.getInstance().currentUser?.metadata?.creationTimestamp ?: 0L
    val joinedText = remember(creationTimeMillis) {
        val daysAgo = ((System.currentTimeMillis() - creationTimeMillis) / (1000 * 60 * 60 * 24)).toInt()
        when (daysAgo) {
            0 -> "Joined today"
            1 -> "Joined 1 day ago"
            else -> "Joined $daysAgo days ago"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Email
        Text(
            text = email,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        // 🔥 Joined Date
        Text(
            text = joinedText,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("landing") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = false
                }

            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Logout", color = Color.White)
        }
    }
}
