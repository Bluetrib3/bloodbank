package com.example.bloodbank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodQuantityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blood Quantity", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F))
            )
        }
    ) { paddingValues ->
        BloodQuantityTable(Modifier.padding(paddingValues))
    }
}

@Composable
fun BloodQuantityTable(modifier: Modifier = Modifier) {
    val bloodData = listOf(
        BloodType("A+", 10),
        BloodType("A-", 5),
        BloodType("B+", 8),
        BloodType("B-", 3),
        BloodType("O+", 12),
        BloodType("O-", 2),
        BloodType("AB+", 7),
        BloodType("AB-", 4)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFEBEE))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Available Blood Units",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Blood Type", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Units", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color.Gray, thickness = 1.dp)

                LazyColumn {
                    items(bloodData) { blood ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(blood.type, fontSize = 16.sp)
                            Text("${blood.units}", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

data class BloodType(val type: String, val units: Int)


