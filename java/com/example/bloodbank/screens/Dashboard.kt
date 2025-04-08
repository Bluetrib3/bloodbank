package com.example.bloodbank

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F), // Blood bank theme (Red)
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        DashboardContent(Modifier.padding(paddingValues), navController)
    }
}

@Composable
fun DashboardContent(modifier: Modifier = Modifier, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    var bloodStock by remember { mutableStateOf(mapOf<String, Int>()) }

    // 🔹 Get the user ID from navigation arguments (ensure you pass userId when navigating)
    val currentUserId = rememberUpdatedState(navController.currentBackStackEntry?.arguments?.getString("userId") ?: "")

    // 🔹 Fetch blood stock whenever user changes
    LaunchedEffect(currentUserId.value) {
        firestore.collection("People")
            .get()
            .addOnSuccessListener { result ->
                val bloodCount = mutableMapOf<String, Int>()
                result.documents.forEach { doc ->
                    val bloodGroup = doc.getString("bloodGroup") ?: ""
                    if (bloodGroup.isNotEmpty()) {
                        bloodCount[bloodGroup] = bloodCount.getOrDefault(bloodGroup, 0) + 1
                    }
                }
                bloodStock = bloodCount
            }
            .addOnFailureListener {
                Toast.makeText(navController.context, "Failed to load blood stock", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBEE)) // Light pinkish background
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to the Blood Bank App!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C) // Dark Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336)) // Brighter Red
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Total Blood Donors: ${bloodStock.values.sum()}",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Blood Quantity by Type",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C)
        )

        Spacer(modifier = Modifier.height(8.dp))

        BloodQuantityTable(bloodStock)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("addpersonpage") }) {
            Text(text = "Add Person")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate("view_people") }) {
            Text("View People History")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Search Button to Navigate to the Search Page
        Button(
            onClick = { navController.navigate("search_page") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) // Green button
        ) {
            Text("Search for a Donor", color = Color.White)
        }
    }
}

@Composable
fun BloodQuantityTable(bloodStock: Map<String, Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Blood Type", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text("Quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }

        Divider(color = Color.White, thickness = 2.dp)

        val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

        bloodTypes.forEach { type ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(type, fontSize = 16.sp, color = Color.White)
                Text(bloodStock[type]?.toString() ?: "0", fontSize = 16.sp, color = Color.White)
            }
            Divider(color = Color.White, thickness = 1.dp)
        }
    }
}
