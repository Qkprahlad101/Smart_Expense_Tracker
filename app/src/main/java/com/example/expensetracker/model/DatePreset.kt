package com.example.expensetracker.model

enum class DatePreset(val label: String) {
    Today("Today"),
    ThisWeek("This Week"),
    ThisMonth("This Month");

    companion object {
        val all = values().toList()
        fun labels(): List<String> = all.map { it.label }
        fun fromLabel(label: String): DatePreset =
            all.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Today
    }
}
