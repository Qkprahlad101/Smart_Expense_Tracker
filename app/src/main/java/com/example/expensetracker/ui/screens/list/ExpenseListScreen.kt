package com.example.expensetracker.ui.screens.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.components.AppScaffold  // Your custom AppScaffold
import com.example.expensetracker.ui.components.DropdownMenuBox
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(navController: NavController) {
    val viewModel: ExpenseListViewModel = koinViewModel()
    var groupBy by remember { mutableStateOf("By Category") }
    var datePreset by remember { mutableStateOf("Today") }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    val (start, end) = viewModel.getDateRange(datePreset)
    LaunchedEffect(datePreset, groupBy) {
        viewModel.getExpenses(groupBy == "By Category", start, end).collectLatest { expenses = it }
    }

    val grouped = viewModel.getGroupedExpenses(groupBy == "By Category", expenses)
    val totalCount = expenses.size
    val totalAmount = expenses.sumOf { it.amount }

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
                    .padding(16.dp)
            ) {
                Text(
                    text = "Expense List",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DropdownMenuBox(selected = datePreset, options = listOf("Today", "This Week", "This Month")) { datePreset = it }
                    DropdownMenuBox(selected = groupBy, options = listOf("By Category", "By Date")) { groupBy = it }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Count: $totalCount", style = MaterialTheme.typography.bodyLarge)
                        Text("Total Amount: $${totalAmount}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (expenses.isEmpty()) {
                    Text("No expenses yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn {
                        grouped.forEach { (key, list) ->
                            item { Text(key, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
                            items(list) { expense ->
                                Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${expense.title}: $${expense.amount}")
                                        Text("(${expense.category})")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text("Add Expense") }
                    Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text("View Report") }
                }
            }
        }
    )
}
