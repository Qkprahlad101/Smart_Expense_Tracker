package com.example.expensetracker.ui.screens.entry

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetracker.ui.components.AppScaffold  // Your custom AppScaffold
import com.example.expensetracker.ui.components.DropdownMenuBox
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(navController: NavController) {
    val viewModel: ExpenseEntryViewModel = koinViewModel()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Staff") }
    var notes by remember { mutableStateOf("") }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var showToast by remember { mutableStateOf(false) }
    var totalToday by remember { mutableStateOf(0.0) }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> receiptUri = uri }

    LaunchedEffect(Unit) {
        viewModel.totalToday.collectLatest { totalToday = it ?: 0.0 }
    }

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Expense Tracker") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Expense Entry",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Total Spent Today: $$totalToday", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                val categories = listOf("Staff", "Travel", "Food", "Utility")
                DropdownMenuBox(category, categories) { category = it }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { if (it.length <= 100) notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { fileLauncher.launch("*/*") }) { Text("Upload Receipt (Image/PDF)") }
                receiptUri?.let { Text("Selected: $it", color = MaterialTheme.colorScheme.secondary) }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    viewModel.addExpense(title, amt, category, notes, receiptUri?.toString())
                    showToast = true
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit")
                }
                AnimatedVisibility(visible = showToast, enter = fadeIn(), exit = fadeOut()) {
                    Text("Expense Added!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { navController.navigate(Destinations.LIST) }) { Text("View List") }
                    Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text("View Report") }
                }
            }
        }
    )
}
