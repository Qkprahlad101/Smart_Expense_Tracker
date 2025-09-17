package com.example.expensetracker.ui.screens.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExpenseReportScreen(navController: NavController) {
    val viewModel: ExpenseReportViewModel = koinViewModel()
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.expensesLast7Days.collectLatest { expenses = it }
    }

    val dailyTotals = viewModel.getDailyTotals(expenses)
    val categoryTotals = viewModel.getCategoryTotals(expenses)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Last 7 Days Report")
        Text("Daily Totals:")
        dailyTotals.forEach { (date, total) -> Text("$date: $$total") }
        Text("Category Totals:")
        categoryTotals.forEach { (cat, total) -> Text("$cat: $$total") }

        Canvas(modifier = Modifier.height(200.dp)) {
            val barWidth = size.width / categoryTotals.size
            categoryTotals.values.forEachIndexed { index, value ->
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(index * barWidth, size.height - (value.toFloat() * 0.5f)), // Scale mock
                    size = Size(barWidth, value.toFloat() * 0.5f)
                )
            }
        }

        Button(onClick = {
            val csv = "Title,Amount,Category\n" + expenses.joinToString("\n") { "${it.title},${it.amount},${it.category}" }
        }) {
            Text("Export CSV")
        }
        Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text("Add Expense") }
        Button(onClick = { navController.navigate(Destinations.LIST) }) { Text("View List") }
    }
}
