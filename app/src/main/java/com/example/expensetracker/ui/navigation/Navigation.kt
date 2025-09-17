package com.example.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetracker.ui.screens.entry.ExpenseEntryScreen
import com.example.expensetracker.ui.screens.list.ExpenseListScreen
import com.example.expensetracker.ui.screens.report.ExpenseReportScreen

object Destinations {
    const val ENTRY = "entry"
    const val LIST = "list"
    const val REPORT = "report"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destinations.ENTRY) {
        composable(Destinations.ENTRY) { ExpenseEntryScreen(navController) }
        composable(Destinations.LIST) { ExpenseListScreen(navController) }
        composable(Destinations.REPORT) { ExpenseReportScreen(navController) }
    }
}