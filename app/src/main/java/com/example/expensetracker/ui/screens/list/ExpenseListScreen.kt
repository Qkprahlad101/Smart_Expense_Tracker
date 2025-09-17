package com.example.expensetracker.ui.screens.list

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.model.Category
import com.example.expensetracker.model.DatePreset
import com.example.expensetracker.ui.AppText
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
    var groupBy by remember { mutableStateOf(AppText.byCategory) }
    var datePreset by remember { mutableStateOf(DatePreset.Today.label) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }

    // State for preview dialog
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    val (start, end) = viewModel.getDateRange(datePreset)
    LaunchedEffect(datePreset, groupBy) {
        viewModel.getExpenses(groupBy == AppText.byCategory, start, end).collectLatest { expenses = it }
    }

    val grouped = viewModel.getGroupedExpenses(groupBy == AppText.byCategory, expenses)
    val totalCount = expenses.size
    val totalAmount = expenses.sumOf { it.amount }

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppText.appTitle) },
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
                    text = AppText.screenList,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DropdownMenuBox(selected = datePreset, options = DatePreset.labels()) { datePreset = it }
                    DropdownMenuBox(selected = groupBy, options = listOf(AppText.byCategory, AppText.byDate)) { groupBy = it }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${AppText.totalCount}: $totalCount", style = MaterialTheme.typography.bodyLarge)
                        Text("${AppText.totalAmount}: ${CurrencyUtil.rupee(totalAmount)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (expenses.isEmpty()) {
                    Text(AppText.noExpenses, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                    Button(onClick = { navController.navigate(Destinations.ENTRY) }) { Text(AppText.addExpense) }
                    Button(onClick = { navController.navigate(Destinations.REPORT) }) { Text(AppText.viewReport) }
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

    // Restore exact working logic: use receiptPath as parsable URI first, then as file path
    val previewUri: Uri? = remember(expense.receiptPath) {
        when {
            !expense.receiptPath.isNullOrBlank() -> {
                // First branch: treat as parsable URI string
                runCatching { Uri.parse(expense.receiptPath) }.getOrNull()
            }
            !expense.receiptPath.isNullOrBlank() -> {
                // Second branch: treat as file path with FileProvider
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

    // Restore exact working image detection
    val isImage: Boolean = remember(previewUri) {
        previewUri?.let { uri ->
            val type = context.contentResolver.getType(uri)
            if (type != null) {
                type.startsWith("image/")
            } else {
                val path = expense.receiptPath.orEmpty().lowercase(Locale.ROOT)
                path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                        path.endsWith(".webp") || path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".heic")
            }
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
