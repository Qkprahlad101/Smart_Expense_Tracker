package com.example.expensetracker.ui.screens.list

import androidx.lifecycle.ViewModel
import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.model.DatePreset
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseListViewModel(private val repository: ExpenseRepository) : ViewModel(), KoinComponent {

    fun getExpenses(groupByCategory: Boolean, start: Long, end: Long): Flow<List<Expense>> {
        return repository.getExpensesForDate(start, end)
    }

    fun getGroupedExpenses(groupByCategory: Boolean, expenses: List<Expense>): Map<String, List<Expense>> {
        return if (groupByCategory) {
            expenses.groupBy { it.category.display }
        } else {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            expenses.groupBy { df.format(it.date) }
        }
    }

    fun getDateRange(presetLabel: String): Pair<Long, Long> {
        val preset = DatePreset.fromLabel(presetLabel)
        val calendar = Calendar.getInstance()
        val end = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        when (preset) {
            DatePreset.Today -> {}
            DatePreset.ThisWeek -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            DatePreset.ThisMonth -> calendar.add(Calendar.MONTH, -1)
        }
        val start = calendar.timeInMillis
        return start to end
    }
}
