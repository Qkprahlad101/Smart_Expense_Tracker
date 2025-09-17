package com.example.expensetracker.ui.screens.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.ui.components.AppScaffold  // Assuming this is your custom AppScaffold
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
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
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

                Spacer(Modifier.height(16.dp))
                Text("Category Totals (Bar Graph):", style = MaterialTheme.typography.titleMedium)
                CategoryTotalsHeader(categoryTotals)   // <-- new header
                Spacer(modifier = Modifier.height(8.dp))
                BarGraph(categoryTotals)  // keep height inside composable
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { shareReport(context, expenses) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export and Share Report")
                }

                Spacer(Modifier.height(24.dp))
            }

        }
    )
}

private fun shareReport(context: Context, expenses: List<Expense>) {
    val csvContent = "Title,Amount(INR),Category\n" +
            expenses.joinToString("\n") { "${it.title},${it.amount},${it.category}" }
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
    val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFFFFC107))
    val entries = totals.entries.toList()
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // a bit taller to make room for labels
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (entries.isEmpty()) return@Canvas

            // Reserve bottom area for labels (in pixels)
            val labelAreaPx = 28f
            val chartHeight = size.height - labelAreaPx

            val maxVal = entries.maxOf { it.value }
            val safeMax = if (maxVal <= 0.0) 1.0 else maxVal
            val barGap = 12f
            val barWidth = (size.width - barGap * (entries.size + 1)) / entries.size.coerceAtLeast(1)
            val minBarPx = 4f

            // Prepare a Paint for text drawing
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.DKGRAY
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 11.sp.toPx() // small label size
            }

            entries.forEachIndexed { index, entry ->
                val ratio = (entry.value / safeMax).toFloat().coerceIn(0f, 1f)
                val barHeight = (ratio * chartHeight).coerceAtLeast(minBarPx)
                val left = barGap + index * (barWidth + barGap)
                val barLeft = left
                val barRight = left + barWidth
                val barTop = chartHeight - barHeight
                val barBottom = chartHeight

                // Draw bar
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barHeight)
                )

                // Compute label (truncate if too long)
                val rawLabel = entry.key
                val maxChars = when {
                    barWidth < 40f -> 5
                    barWidth < 60f -> 7
                    else -> 10
                }
                val label = if (rawLabel.length > maxChars) rawLabel.take(maxChars - 1) + "…" else rawLabel

                // Draw label centered under the bar
                val labelCenterX = (barLeft + barRight) / 2f
                val baselineY = size.height - 6f // a bit above bottom
                drawContext.canvas.nativeCanvas.drawText(label, labelCenterX, baselineY, paint)
            }
        }
    }
}

@Composable
fun CategoryTotalsHeader(categoryTotals: Map<String, Double>) {
    val ordered = categoryTotals.toList().sortedBy { it.first }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ordered.forEach { (category, total) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$category:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = com.example.expensetracker.ui.components.CurrencyUtil.rupee(total),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}




