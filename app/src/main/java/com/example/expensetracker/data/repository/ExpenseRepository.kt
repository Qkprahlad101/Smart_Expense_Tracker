package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.Expense
import com.example.expensetracker.data.database.ExpenseDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val dao: ExpenseDao) {

    suspend fun addExpense(expense: Expense) {
        dao.insert(expense)
    }

    fun getExpensesForToday(): Flow<List<Expense>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return dao.getExpensesForDateRange(start, end)
    }

    fun getExpensesForDate(start: Long, end: Long): Flow<List<Expense>> {
        return dao.getExpensesForDateRange(start, end)
    }

    fun getTotalForToday(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis
        return dao.getTotalForDateRange(start, end)
    }

    fun getAllExpenses(): Flow<List<Expense>> = dao.getAllExpenses()

    fun getExpensesForLast7Days(): Flow<List<Expense>> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val start = calendar.timeInMillis
        val end = System.currentTimeMillis()
        return dao.getExpensesForDateRange(start, end)
    }
}
