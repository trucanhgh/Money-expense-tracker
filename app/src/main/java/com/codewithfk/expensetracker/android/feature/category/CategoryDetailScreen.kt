package com.codewithfk.expensetracker.android.feature.category

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.feature.home.TransactionList
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CategoryDetailContent(
    name: String,
    getExpensesForCategory: (name: String, month: String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>
) {
    // Filter by month: selectedMonth == null => show all; otherwise format MM/YYYY
    val selectedMonth = remember { mutableStateOf<String?>(null) }
    val currentMonth = remember { getCurrentMonthString() }
    val expensesFlow = getExpensesForCategory(name, selectedMonth.value)
    val expenses by expensesFlow.collectAsState(initial = emptyList())
    val showFilterDialog = remember { mutableStateOf(false) }
    // prepare last 12 months list for selection
    fun last12Months(): List<String> {
        val months = mutableListOf<String>()
        val cal = java.util.Calendar.getInstance()
        repeat(12) {
            val m = cal.get(java.util.Calendar.MONTH) + 1
            val y = cal.get(java.util.Calendar.YEAR)
            months.add(String.format(java.util.Locale.getDefault(), "%02d/%d", m, y))
            cal.add(java.util.Calendar.MONTH, -1)
        }
        return months
    }

    // If name is blank show a friendly message
    if (name.isBlank()) {
        Scaffold(topBar = {}) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExpenseTextView(text = "Danh mục không hợp lệ", color = Color.Black)
                }
            }
        }
        return
    }

    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header label (larger & bold) and the actual name (smaller, not bold)
                ExpenseTextView(text = "Danh mục", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.size(6.dp))
                // Actual category name: smaller and not bold (single-line with ellipsis)
                ExpenseTextView(text = name, style = MaterialTheme.typography.titleLarge, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.size(8.dp))

                // Filter button opens inline filter panel
                Button(onClick = { showFilterDialog.value = !showFilterDialog.value }) {
                    ExpenseTextView(text = "Lọc")
                }

                Spacer(modifier = Modifier.size(12.dp))

                // Inline filter panel: when active, show a full-width Surface (white) with black text
                if (showFilterDialog.value) {
                    val months = last12Months()
                    Surface(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), color = Color.White) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ExpenseTextView(text = "Lọc giao dịch", fontWeight = androidx.compose.material3.MaterialTheme.typography.titleLarge.fontWeight, color = Color.Black)
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(onClick = {
                                selectedMonth.value = null
                                showFilterDialog.value = false
                            }) { ExpenseTextView(text = "Hiển thị tất cả") }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(onClick = {
                                selectedMonth.value = currentMonth
                                showFilterDialog.value = false
                            }) { ExpenseTextView(text = "Tháng này") }
                            Spacer(modifier = Modifier.size(8.dp))
                            // Quick pick recent months
                            months.forEach { m ->
                                Spacer(modifier = Modifier.size(6.dp))
                                Button(onClick = {
                                    selectedMonth.value = m
                                    showFilterDialog.value = false
                                }) { ExpenseTextView(text = m, color = Color.Black) }
                            }
                        }
                    }
                }

                if (expenses.isEmpty()) {
                    ExpenseTextView(text = "Không có giao dịch", color = Color.Black)
                } else {
                    TransactionList(modifier = Modifier.fillMaxWidth(), list = expenses, title = "Giao dịch") { }
                }
            }
        }
    }

    // Removed the previous AlertDialog-based filter UI in favor of inline panel above
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun CategoryDetailScreen(
    navController: NavController,
    encodedName: String?,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val nameRaw = encodedName?.let { Uri.decode(it) } ?: ""
    val name = nameRaw.trim()

    CategoryDetailContent(name = name, getExpensesForCategory = { n, m -> viewModel.getExpensesForCategory(n, m) })
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryDetailContent() {
    val sampleExpenses = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 1, title = "Ăn trưa", amount = 120000.0, date = "01/12/2025", type = "Expense"),
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 2, title = "Cà phê", amount = 30000.0, date = "02/12/2025", type = "Expense")
    )
    ExpenseTrackerAndroidTheme {
        CategoryDetailContent(name = "Ăn uống", getExpensesForCategory = { _, _ -> flowOf(sampleExpenses) })
    }
}
