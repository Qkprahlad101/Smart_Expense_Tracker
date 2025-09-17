package com.example.expensetracker.model

import androidx.compose.ui.graphics.Color

enum class Category(val display: String, val bg: Color, val fg: Color) {
    Staff(
        display = "Staff",
        bg = Color(0xFFE8F5E9),
        fg = Color(0xFF2E7D32)
    ),
    Travel(
        display = "Travel",
        bg = Color(0xFFE3F2FD),
        fg = Color(0xFF1565C0)
    ),
    Food(
        display = "Food",
        bg = Color(0xFFFFF3E0),
        fg = Color(0xFFEF6C00)
    ),
    Utility(
        display = "Utility",
        bg = Color(0xFFF3E5F5),
        fg = Color(0xFF6A1B9A)
    ),
    Other(
        display = "Other",
        bg = Color(0xFFF5F5F5),
        fg = Color(0xFF424242)
    );

    companion object {
        fun fromLabelOrNull(label: String?): Category? {
            if (label.isNullOrBlank()) return null
            // Try enum name
            values().firstOrNull { it.name.equals(label, ignoreCase = true) }?.let { return it }
            // Try display
            return values().firstOrNull { it.display.equals(label, ignoreCase = true) }
        }
        fun fromLabel(label: String?): Category = fromLabelOrNull(label) ?: Other
        val default = Staff
        val all = values().toList()
        fun labels(): List<String> = all.map { it.display }
    }
}
