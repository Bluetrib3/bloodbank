package com.example.bloodbank

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddPersonPage(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    var isLoading by remember { mutableStateOf(false) }

    fun validateInputs(): Boolean {
        var isValid = true

        if (!name.matches(Regex("^[a-zA-Z\\s]+$"))) {
            nameError = "Name should contain only letters"
            isValid = false
        } else {
            nameError = null
        }

        if (!mobile.matches(Regex("^\\d{10}$"))) {
            mobileError = "Enter a valid 10-digit mobile number"
            isValid = false
        } else {
            mobileError = null
        }

        val ageInt = age.toIntOrNull()
        if (ageInt == null || ageInt !in 18..60) {
            ageError = "Age should be between 18 and 60"
            isValid = false
        } else {
            ageError = null
        }

        return isValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Person Details",
                        fontSize = 22.sp,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = nameError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                nameError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = mobileError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                mobileError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ageError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                ageError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }

                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedBloodGroup.isEmpty()) "Select Blood Group" else selectedBloodGroup)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        bloodGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedBloodGroup = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!validateInputs()) return@Button
                        isLoading = true

                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val userId = currentUser?.uid

                        if (userId == null) {
                            isLoading = false
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val personData = hashMapOf(
                            "name" to name,
                            "address" to address,
                            "mobile" to mobile,
                            "age" to age,
                            "bloodGroup" to selectedBloodGroup,
                            "timestamp" to Timestamp.now(),
                            "userId" to userId // link to the current user
                        )

                        firestore.collection("People").add(personData)
                            .addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "Details Saved", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Details", color = Color.White)
                    }
                }
            }
        }
    }
}
