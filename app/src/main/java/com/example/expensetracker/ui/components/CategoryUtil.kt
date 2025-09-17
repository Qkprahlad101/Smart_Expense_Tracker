package com.example.expensetracker.ui.components

import com.example.expensetracker.model.Category

object CategoryUtil {
    fun bg(category: Category) = category.bg
    fun fg(category: Category) = category.fg
    fun display(category: Category) = category.display
}
