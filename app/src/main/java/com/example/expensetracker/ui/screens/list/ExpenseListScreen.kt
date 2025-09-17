package com.example.expensetracker.ui.screens.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExpenseListScreen(navController: NavController) {
    val viewModel: ExpenseListViewModel = koinViewModel()
    var groupByCategory by remember { mutableStateOf(false) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    val (start, end) = viewModel.getTodayRange()


    LaunchedEffect(Unit) {
        viewModel.getExpenses(groupByCategory, start, end).collectLatest { expenses = it }
    }

    val grouped = viewModel.getGroupedExpenses(groupByCategory, expenses)
    val totalCount = expenses.size
    val totalAmount = expenses.sumOf { it.amount }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Total Count: $totalCount, Total Amount: $${totalAmount}")
        Button(onClick = { groupByCategory = !groupByCategory }) {
            Text(if (groupByCategory) "Group by Time" else "Group by Category")
        }
        if (expenses.isEmpty()) {
            Text("No expenses yet.")
        } else {
            LazyColumn {
                grouped.forEach { (key, list) ->
                    item { Text(key) }
                    items(list) { expense ->
                        Text("${expense.title}: $${expense.amount} (${expense.category})")
                    }
                }
            }
        }
        Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text("Add Expense") }
        Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text("View Report") }
    }
}
