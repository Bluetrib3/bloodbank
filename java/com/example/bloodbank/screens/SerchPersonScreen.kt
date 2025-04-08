package com.example.bloodbank

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun SearchPersonScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Text(
            text = "Search for Blood Donors",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Enter Name, Blood Group, or Mobile Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Button
        Button(
            onClick = {
                searchDonors(searchQuery, firestore) { results ->
                    searchResults = results
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Search Results
        LazyColumn {
            items(searchResults) { person ->
                PersonCard(person, firestore) { updatedList ->
                    searchResults = updatedList
                }
            }
        }
    }
}

// Function to Fetch Matching Donors from Firestore
fun searchDonors(query: String, firestore: FirebaseFirestore, onResult: (List<Map<String, String>>) -> Unit) {
    if (query.isBlank()) {
        onResult(emptyList())
        return
    }

    firestore.collection("People")
        .orderBy("name", Query.Direction.ASCENDING)
        .get()
        .addOnSuccessListener { result ->
            val matchedPeople = result.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: ""
                val bloodGroup = doc.getString("bloodGroup") ?: ""
                val mobile = doc.getString("mobile") ?: ""

                if (name.contains(query, ignoreCase = true) ||
                    bloodGroup.equals(query, ignoreCase = true) ||
                    mobile.contains(query)
                ) {
                    mapOf("id" to id, "name" to name, "bloodGroup" to bloodGroup, "mobile" to mobile)
                } else {
                    null
                }
            }
            onResult(matchedPeople)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

// UI for Displaying Each Person with Edit & Delete Options
@Composable
fun PersonCard(person: Map<String, String>, firestore: FirebaseFirestore, onUpdate: (List<Map<String, String>>) -> Unit) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${person["name"]}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Blood Group: ${person["bloodGroup"]}", fontSize = 16.sp)
            Text(text = "Mobile: ${person["mobile"]}", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons for Edit and Delete
            Row {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        deletePerson(person["id"] ?: "", firestore, context, onUpdate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        EditPersonDialog(person, firestore, onUpdate) { showEditDialog = false }
    }
}

// Function to Delete a Person from Firestore
fun deletePerson(id: String, firestore: FirebaseFirestore, context: android.content.Context, onUpdate: (List<Map<String, String>>) -> Unit) {
    firestore.collection("People").document(id)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            searchDonors("", firestore, onUpdate)  // Refresh the list
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
        }
}

// Edit Dialog for Updating Person Details
@Composable
fun EditPersonDialog(person: Map<String, String>, firestore: FirebaseFirestore, onUpdate: (List<Map<String, String>>) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(person["name"] ?: "") }
    var bloodGroup by remember { mutableStateOf(person["bloodGroup"] ?: "") }
    var mobile by remember { mutableStateOf(person["mobile"] ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Details") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") })
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") })
            }
        },
        confirmButton = {
            Button(onClick = {
                updatePerson(person["id"] ?: "", name, bloodGroup, mobile, firestore, context, onUpdate)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Function to Update a Person's Details in Firestore
fun updatePerson(id: String, name: String, bloodGroup: String, mobile: String, firestore: FirebaseFirestore, context: android.content.Context, onUpdate: (List<Map<String, String>>) -> Unit) {
    val updatedData = mapOf("name" to name, "bloodGroup" to bloodGroup, "mobile" to mobile)

    firestore.collection("People").document(id)
        .update(updatedData)
        .addOnSuccessListener {
            Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            searchDonors("", firestore, onUpdate)  // Refresh the list
        }
        .addOnFailureListener {
            Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
        }
}
