package com.example.bloodbank

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginPage(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Light gray background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.bloodbank_logo), // Add your logo
                contentDescription = "Blood Bank Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Title
            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            // Email Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFFD32F2F),
                    focusedTextColor = Color.Black,  // Set text color to black
                    unfocusedTextColor = Color.Black // Set text color to black
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFFD32F2F),
                    focusedTextColor = Color.Black,  // Set text color to black
                    unfocusedTextColor = Color.Black // Set text color to black
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp)
            )

            // Login Button
            Button(
                onClick = {
                    isLoading = true
                    auth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") // Navigate to Home Screen
                            } else {
//                                Log.d("ERROR", task.exception?.message)
                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(12.dp)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Login", fontSize = 18.sp, color = Color.White)
                }
            }

            // Register Text
            TextButton(
                onClick = { navController.navigate("register") }, // Navigate to Register
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Don't have an account? Register", color = Color(0xFFD32F2F), fontSize = 14.sp)
            }
        }
    }
}
