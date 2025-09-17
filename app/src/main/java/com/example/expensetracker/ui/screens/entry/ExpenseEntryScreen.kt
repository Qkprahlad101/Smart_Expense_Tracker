package com.example.expensetracker.ui.screens.entry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExpenseEntryScreen(navController: NavController) {
    val viewModel: ExpenseEntryViewModel = koinViewModel()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Staff") }
    var notes by remember { mutableStateOf("") }
    var receipt by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    var totalToday by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        viewModel.totalToday.collectLatest { totalToday = it ?: 0.0 }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Total Spent Today: $$totalToday")
        TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        TextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
        val categories = listOf("Staff", "Travel", "Food", "Utility")
        DropdownMenuBox(category, categories) { category = it }
        TextField(value = notes, onValueChange = { if (it.length <= 100) notes = it }, label = { Text("Notes") })
        TextField(value = receipt, onValueChange = { receipt = it }, label = { Text("Receipt Image (mock)") })
        Button(onClick = {
            val amt = amount.toDoubleOrNull() ?: 0.0
            viewModel.addExpense(title, amt, category, notes, receipt)
            showToast = true
        }) {
            Text("Submit")
        }
        AnimatedVisibility(visible = showToast, enter = fadeIn(), exit = fadeOut()) {
            Text("Expense Added!")
        }
        Button(onClick = { navController.navigate(Destinations.LIST) }) { Text("View List") }
        Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text("View Report") }
    }
}

@Composable
fun DropdownMenuBox(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelect(option)
                    expanded = false
                })
            }
        }
    }
}
