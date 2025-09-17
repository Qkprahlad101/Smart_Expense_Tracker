package com.example.expensetracker.ui.screens.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.components.AppScaffold  // Assuming this is your custom AppScaffold
import com.example.expensetracker.ui.navigation.Destinations
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(navController: NavController) {
    val viewModel: ExpenseReportViewModel = koinViewModel()
    val context = LocalContext.current
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.expensesLast7Days.collectLatest { expenses = it }
    }

    val dailyTotals = viewModel.getDailyTotals(expenses)
    val categoryTotals = viewModel.getCategoryTotals(expenses)

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
                    text = "Expense Report",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Last 7 Days Report", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Category Totals (Bar Graph):", style = MaterialTheme.typography.titleMedium)
                BarGraph(categoryTotals)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Daily Totals (Line Graph):", style = MaterialTheme.typography.titleMedium)
                LineGraph(dailyTotals)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    shareReport(context, expenses)
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Export and Share Report")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text("Add Expense") }
                    Button(onClick = { navController.navigate(Destinations.LIST) }) { Text("View List") }
                }
            }
        }
    )
}

private fun shareReport(context: Context, expenses: List<Expense>) {
    val csvContent = "Title,Amount,Category\n" + expenses.joinToString("\n") { "${it.title},${it.amount},${it.category}" }
    val csvFile = File(context.cacheDir, "report.csv")
    csvFile.writeText(csvContent)
    val csvUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", csvFile)

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    intent.type = "*/*"
    val uris = arrayListOf(csvUri)
    expenses.forEach { expense ->
        expense.receiptPath?.let { uriStr ->
            val uri = Uri.parse(uriStr)
            uris.add(uri)
            context.grantUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}

@Composable
fun BarGraph(totals: Map<String, Double>) {
    val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFFFFEB3B))
    val max = totals.values.maxOrNull() ?: 1.0
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val barWidth = size.width / totals.size
            totals.entries.forEachIndexed { index, entry ->
                val height = (entry.value / max).toFloat() * size.height
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(index * barWidth, size.height - height),
                    size = Size(barWidth - 8f, height)
                )
            }
        }
    }
}

@Composable
fun LineGraph(totals: Map<String, Double>) {
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val max = totals.values.maxOrNull() ?: 1.0  // Keep as Double
            val points = totals.entries.sortedBy { it.key }.mapIndexed { index, entry ->
                val x = index * (size.width / (totals.size - 1))
                val ratio = entry.value / max
                val scaledHeight = (ratio * size.height.toDouble()).toFloat()
                val y = size.height - scaledHeight
                Offset(x, y)
            }
            for (i in 0 until points.size - 1) {
                drawLine(Color(0xFF673AB7), points[i], points[i + 1], strokeWidth = 4f)
            }
            points.forEach { drawCircle(Color.Black, radius = 6f, center = it) }
        }
    }
}
