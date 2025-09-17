package com.example.expensetracker.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.expensetracker.model.Category

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: Category = Category.default,
    val notes: String? = null,
    val receiptUri: String? = null,
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
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
