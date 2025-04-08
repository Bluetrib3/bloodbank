package com.example.bloodbank

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPeoplePage(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var peopleList by remember { mutableStateOf(listOf<Person>()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasLoadedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchHistoryRealtime(firestore) { fetchedPeople ->
            if (fetchedPeople.isNotEmpty() || peopleList.isEmpty()) {
                peopleList = fetchedPeople
            }
            if (!hasLoadedOnce) {
                isLoading = false
                hasLoadedOnce = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { printToPDF(context, firestore) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Print PDF")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        deleteAllPeople(context, firestore)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete All", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.White
                    )
                }
                !isLoading && peopleList.isEmpty() -> {
                    Text(
                        "No donors yet.",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    LazyColumn {
                        items(peopleList) { person ->
                            PersonCard(person)
                        }
                    }
                }
            }
        }
    }
}

fun fetchHistoryRealtime(firestore: FirebaseFirestore, onResult: (List<Person>) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    firestore.collection("People")
        .whereEqualTo("userId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreError", "Listen failed: ${e.message}")
                onResult(emptyList())
                return@addSnapshotListener
            }
            val people = snapshot?.documents?.mapNotNull { doc ->
                val timestamp = doc.getTimestamp("timestamp")
                val formattedDate = timestamp?.toDate()?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(it)
                } ?: "Unknown Date"
                Person(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    address = doc.getString("address") ?: "",
                    mobile = doc.getString("mobile") ?: "",
                    age = doc.getString("age") ?: "",
                    bloodGroup = doc.getString("bloodGroup") ?: "",
                    timestamp = formattedDate
                )
            } ?: emptyList()

            onResult(people)
        }
}

fun printToPDF(context: Context, firestore: FirebaseFirestore) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    val userName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Unknown User"

    if (userId == null) {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        return
    }

    firestore.collection("People")
        .whereEqualTo("userId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { result ->
            val peopleList = result.documents.mapNotNull { doc ->
                try {
                    val timestamp = doc.getTimestamp("timestamp")
                    val formattedDate = timestamp?.toDate()?.let {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(it)
                    } ?: "Unknown Date"

                    Person(
                        id = doc.id,
                        name = doc.getString("name") ?: "N/A",
                        address = doc.getString("address") ?: "N/A",
                        mobile = doc.getString("mobile") ?: "N/A",
                        age = doc.getString("age") ?: "N/A",
                        bloodGroup = doc.getString("bloodGroup") ?: "N/A",
                        timestamp = formattedDate
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (peopleList.isEmpty()) {
                Toast.makeText(context, "No data found to print!", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF(context, peopleList, userName)
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
        }
}

fun generatePDF(context: Context, peopleList: List<Person>, userName: String) {
    try {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "PeopleDetails_${System.currentTimeMillis()}.pdf"
        )

        val pdfWriter = PdfWriter(FileOutputStream(file))
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        document.add(Paragraph("🩸 People Details History").setBold().setFontSize(20f))
        document.add(Paragraph("Generated by: $userName").setFontSize(14f))
        document.add(Paragraph("\n"))

        peopleList.forEach { person ->
            document.add(Paragraph("Name: ${person.name}"))
            document.add(Paragraph("Address: ${person.address}"))
            document.add(Paragraph("Mobile: ${person.mobile}"))
            document.add(Paragraph("Age: ${person.age}"))
            document.add(Paragraph("Blood Group: ${person.bloodGroup}"))
            document.add(Paragraph("Added On: ${person.timestamp}"))
            document.add(Paragraph("\n----------------------\n"))
        }

        document.close()
        pdfDocument.close()

        Toast.makeText(context, "✅ PDF saved at:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun deleteAllPeople(context: Context, firestore: FirebaseFirestore) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    firestore.collection("People")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                Toast.makeText(context, "No data to delete!", Toast.LENGTH_SHORT).show()
            } else {
                val batch = firestore.batch()
                for (doc in result.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "✅ All records deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

@Composable
fun PersonCard(person: Person) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${person.name}", fontSize = 18.sp, color = Color.White)
            Text("Address: ${person.address}", fontSize = 14.sp, color = Color.LightGray)
            Text("Mobile: ${person.mobile}", fontSize = 14.sp, color = Color.LightGray)
            Text("Age: ${person.age}", fontSize = 14.sp, color = Color.LightGray)
            Text("Blood Group: ${person.bloodGroup}", fontSize = 14.sp, color = Color.Red)
            Text("Added On: ${person.timestamp}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

data class Person(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val mobile: String = "",
    val age: String = "",
    val bloodGroup: String = "",
    val timestamp: String = ""
)
