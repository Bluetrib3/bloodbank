package com.example.bloodbank
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloodbank.R

@Composable
fun LandingPage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Light gray background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Blood Bank Logo
            Image(
                painter = painterResource(id = R.drawable.bloodbank_logo), // Add your logo in res/drawable
                contentDescription = "Blood Bank Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Welcome Text
            Text(
                text = "Save Lives, Donate Blood",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F), // Red color
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Login Button
            Button(
                onClick = { navController.navigate("login") },  // Navigate to Login Screen
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(text = "Login", fontSize = 18.sp, color = Color.White)
            }

            // Register Button
            OutlinedButton(
                onClick = { navController.navigate("register") }, // Navigate to Register Screen
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = BorderStroke(2.dp, Color(0xFFD32F2F)), // Corrected border implementation
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(text = "Register", fontSize = 18.sp)
            }
        }
    }
}
