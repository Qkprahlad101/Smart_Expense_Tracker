package com.example.expensetracker.ui.screens.list

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.components.AppScaffold
import com.example.expensetracker.ui.components.CurrencyUtil
import com.example.expensetracker.ui.components.DropdownMenuBox
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(navController: NavController) {
    val viewModel: ExpenseListViewModel = koinViewModel()
    var groupBy by remember { mutableStateOf("By Category") }
    var datePreset by remember { mutableStateOf("Today") }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    // State for preview dialog
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

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
                        Text("Total Amount: ${CurrencyUtil.rupee(totalAmount)}", style = MaterialTheme.typography.bodyLarge)
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
                                ExpenseRow(
                                    expense = expense,
                                    onClick = { selectedExpense = expense }
                                )
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

            // Preview dialog
            selectedExpense?.let { exp ->
                ExpensePreviewDialog(expense = exp, onDismiss = { selectedExpense = null })
            }
        }
    )
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${expense.title}: ${CurrencyUtil.rupee(expense.amount)}")
            Text("(${expense.category})")
        }
    }
}

@Composable
private fun ExpensePreviewDialog(expense: Expense, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dateText = remember(expense.date) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(expense.date))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Expense Preview") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Title: ${expense.title}")
                Text("Amount: ${CurrencyUtil.rupee(expense.amount)}")
                Text("Category: ${expense.category}")
                if (!expense.notes.isNullOrBlank()) Text("Notes: ${expense.notes}")
                Text("Date: $dateText")
                if (!expense.receiptPath.isNullOrBlank()) {
                    Text("Receipt: Attached")
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        // Try to open the receipt via FileProvider
                        val file = File(expense.receiptPath!!)
                        if (file.exists()) {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(openIntent) }
                        }
                    }) {
                        Text("Open Receipt")
                    }
                } else if (!expense.receiptPath.isNullOrBlank()) {
                    // If your model uses persisted Uri instead of path:
                    val persistedUri = Uri.parse(expense.receiptPath)
                    Text("Receipt: Attached")
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = persistedUri
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching { context.startActivity(openIntent) }
                    }) {
                        Text("Open Receipt")
                    }
                } else {
                    Text("Receipt: Not attached")
                }
            }
        }
    )
}
