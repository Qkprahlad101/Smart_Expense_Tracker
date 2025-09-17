package com.example.expensetracker.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val notes: String? = null,
    val receiptPath: String? = null,
    val date: Long = System.currentTimeMillis()
)

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses WHERE date >= :start AND date < :end")
    fun getExpensesForDateRange(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :start AND date < :end")
    fun getTotalForDateRange(start: Long, end: Long): Flow<Double?>

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<Expense>>
}

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
