package com.example.expensetracker.di

import android.app.Application
import androidx.room.Room
import com.example.expensetracker.data.database.ExpenseDatabase
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.ui.screens.entry.ExpenseEntryViewModel
import com.example.expensetracker.ui.screens.list.ExpenseListViewModel
import com.example.expensetracker.ui.screens.report.ExpenseReportViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {
    val module = module {
        single { provideDatabase(androidApplication()) }
        single { get<ExpenseDatabase>().expenseDao() }
        single { ExpenseRepository(get()) }

        viewModel { ExpenseEntryViewModel(get()) }
        viewModel { ExpenseListViewModel(get()) }
        viewModel { ExpenseReportViewModel(get()) }
    }

    private fun provideDatabase(app: Application): ExpenseDatabase {
        return Room.databaseBuilder(app, ExpenseDatabase::class.java, "expense_db")
            .build()
    }
}
