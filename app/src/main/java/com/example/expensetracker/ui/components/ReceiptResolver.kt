package com.example.expensetracker.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expensetracker.data.database.Expense
import java.io.File
import java.util.Locale

object ReceiptResolver {
    fun previewUri(context: Context, expense: Expense): Uri? {
        when {
            !expense.receiptPath.isNullOrBlank() -> {
                return runCatching { Uri.parse(expense.receiptPath) }.getOrNull()
            }
            !expense.receiptPath.isNullOrBlank() -> { // Second branch for FileProvider
                val file = File(expense.receiptPath!!)
                if (file.exists()) {
                    return runCatching {
                        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    }.getOrNull()
                } else null
            }
            else -> null
        }
        return null
    }

    fun isImage(context: Context, expense: Expense): Boolean {
        val uri = previewUri(context, expense) ?: return false
        val type = context.contentResolver.getType(uri)
        if (type != null) {
            return type.startsWith("image/")
        } else {
            val path = expense.receiptPath.orEmpty().lowercase(Locale.ROOT)
            return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                    path.endsWith(".webp") || path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".heic")
        }
    }
}
