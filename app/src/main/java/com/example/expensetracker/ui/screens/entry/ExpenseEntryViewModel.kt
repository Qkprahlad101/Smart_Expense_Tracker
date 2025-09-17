package com.example.expensetracker.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ExpenseEntryViewModel(private val repository: ExpenseRepository) : ViewModel(), KoinComponent {

    val totalToday: Flow<Double?> = repository.getTotalForToday()

    fun addExpense(title: String, amount: Double, category: String, notes: String?, receipt: String?) {
        if (title.isBlank() || amount <= 0) return // Validation
        viewModelScope.launch {
            repository.addExpense(Expense(title = title, amount = amount, category = category, notes = notes, receiptImage = receipt))
        }
    }
}
