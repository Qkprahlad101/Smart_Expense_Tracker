package com.example.expensetracker.ui.screens.list

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DropdownMenuBox(
                        selected = datePreset,
                        options = listOf("Today", "This Week", "This Month")
                    ) { datePreset = it }
                    DropdownMenuBox(
                        selected = groupBy,
                        options = listOf("By Category", "By Date")
                    ) { groupBy = it }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Count: $totalCount", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Total Amount: ${CurrencyUtil.rupee(totalAmount)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No expenses yet.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7F7))
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        grouped.forEach { (key, list) ->
                            item {
                                CategoryHeader(title = key)
                            }
                            items(list, key = { it.id }) { expense ->
                                ExpenseRow(
                                    expense = expense,
                                    onClick = { selectedExpense = expense }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text("Add Expense") }
                    Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text("View Report") }
                }
            }

            selectedExpense?.let { exp ->
                ExpensePreviewDialog(expense = exp, onDismiss = { selectedExpense = null })
            }
        }
    )
}

private fun categoryColors(category: String): Pair<Color, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "staff"   -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // light green bg, dark green text
        "travel"  -> Color(0xFFE3F2FD) to Color(0xFF1565C0) // light blue bg, blue text
        "food"    -> Color(0xFFFFF3E0) to Color(0xFFEF6C00) // light orange bg, orange text
        "utility" -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A) // light purple bg, purple text
        else      -> Color(0xFFF5F5F5) to Color(0xFF424242) // neutral
    }
}

@Composable
private fun CategoryHeader(title: String) {
    val (bg, fg) = categoryColors(title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg
        )
    }
}


@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    val (bg, fg) = categoryColors(expense.category)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "rowAlpha")
    val offsetY by animateDpAsState(if (visible) 0.dp else 8.dp, label = "rowOffset")

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .offset(y = offsetY)
            .graphicsLayer { this.alpha = alpha }
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(bg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${expense.title}: ${CurrencyUtil.rupee(expense.amount)}",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "(${expense.category})",
                color = fg,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

/* ---------- Preview dialog with image thumbnail ---------- */

@Composable
private fun ExpensePreviewDialog(expense: Expense, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dateText = remember(expense.date) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(expense.date))
    }

    val previewUri: Uri? = remember(expense.receiptPath, expense.receiptPath) {
        when {
            // If you store a persisted content Uri string
            !expense.receiptPath.isNullOrBlank() -> runCatching { Uri.parse(expense.receiptPath) }.getOrNull()
            // If you store an internal absolute file path
            !expense.receiptPath.isNullOrBlank() -> {
                val file = File(expense.receiptPath!!)
                if (file.exists()) {
                    runCatching {
                        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    }.getOrNull()
                } else null
            }
            else -> null
        }
    }

    val isImage: Boolean = remember(previewUri) {
        previewUri?.let { uri ->
            val type = context.contentResolver.getType(uri)
            type?.startsWith("image/") == true
        } ?: false
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

                when {
                    previewUri == null -> {
                        Text("Receipt: Not attached or unavailable")
                    }
                    isImage -> {
                        Text("Receipt: Attached")
                        ReceiptThumbnail(uri = previewUri)
                        TextButton(onClick = {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = previewUri
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(openIntent) }
                        }) { Text("Open Receipt") }
                    }
                    else -> {
                        val label = context.contentResolver.getType(previewUri)?.uppercase(Locale.ROOT) ?: "FILE"
                        Text("Receipt: Attached")
                        NonImageBadge(label = label)
                        TextButton(onClick = {
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(previewUri, context.contentResolver.getType(previewUri) ?: "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(openIntent) }
                        }) { Text("Open Receipt") }
                    }
                }
            }
        }
    )
}

@Composable
private fun ReceiptThumbnail(uri: Uri) {
    AsyncImage(
        model = uri,
        contentDescription = "Receipt preview",
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun NonImageBadge(label: String) {
    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.take(16),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
