package com.example.expensetracker.ui.screens.report

import androidx.lifecycle.ViewModel
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent

class ExpenseReportViewModel(private val repository: ExpenseRepository) : ViewModel(), KoinComponent {

    val expensesLast7Days: Flow<List<Expense>> = repository.getExpensesForLast7Days()

    fun getDailyTotals(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { java.text.SimpleDateFormat("yyyy-MM-dd").format(it.date) }
            .mapValues { it.value.sumOf { e -> e.amount } }
    }

    fun getCategoryTotals(expenses: List<Expense>): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { e -> e.amount } }
    }
}
