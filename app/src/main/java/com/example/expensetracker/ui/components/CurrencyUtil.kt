package com.example.expensetracker.ui.components

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtil {
    private val indiaLocale = Locale("en", "IN")
    private val formatter: NumberFormat = NumberFormat.getCurrencyInstance(indiaLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun rupee(amount: Double?): String {
        val a = amount ?: 0.0
        return formatter.format(a)
    }
}
