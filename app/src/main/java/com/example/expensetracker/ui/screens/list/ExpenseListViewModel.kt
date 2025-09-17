package com.example.expensetracker.ui.screens.list

import androidx.lifecycle.ViewModel
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import java.util.Calendar

class ExpenseListViewModel(private val repository: ExpenseRepository) : ViewModel(), KoinComponent {

    fun getExpenses(groupByCategory: Boolean, start: Long, end: Long): Flow<List<Expense>> {
        return repository.getExpensesForDate(start, end)
    }

    fun getGroupedExpenses(groupByCategory: Boolean, expenses: List<Expense>): Map<String, List<Expense>> {
        return if (groupByCategory) {
            expenses.groupBy { it.category }
        } else {
            expenses.groupBy { java.text.SimpleDateFormat("yyyy-MM-dd").format(it.date) }
        }
    }

    fun getDateRange(preset: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis + (24 * 60 * 60 * 1000)  // End of current day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        var start = calendar.timeInMillis

        when (preset) {
            "This Week" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "This Month" -> calendar.add(Calendar.MONTH, -1)
            else -> {}
        }
        start = calendar.timeInMillis
        return start to end
    }

    fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return start to end
    }
}
