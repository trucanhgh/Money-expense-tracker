package com.codewithfk.expensetracker.android.feature.category

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.feature.home.TransactionList
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun CategoryDetailContent(
    name: String,
    getExpensesForCategory: (name: String, month: String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>
) {
    // Mặc định hiển thị tất cả giao dịch (không chỉ trong tháng này)
    val monthFilter = remember { mutableStateOf(false) }
    val month = remember { getCurrentMonthString() }
    val expensesFlow = getExpensesForCategory(name, if (monthFilter.value) month else null)
    val expenses by expensesFlow.collectAsState(initial = emptyList())

    // If name is blank show a friendly message
    if (name.isBlank()) {
        Scaffold(topBar = {}) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExpenseTextView(text = "Danh mục không hợp lệ")
                }
            }
        }
        return
    }

    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                ExpenseTextView(text = name)
                Spacer(modifier = Modifier.size(8.dp))

                // Chuyển đổi bộ lọc: Tất cả thời gian so với Tháng này
                Button(onClick = { monthFilter.value = !monthFilter.value }) {
                    ExpenseTextView(text = if (monthFilter.value) "Tháng này" else "Tất cả")
                }

                Spacer(modifier = Modifier.size(12.dp))

                if (expenses.isEmpty()) {
                    ExpenseTextView(text = "Không có giao dịch")
                } else {
                    TransactionList(modifier = Modifier.fillMaxWidth(), list = expenses, title = "Giao dịch") { }
                }
            }
        }
    }
}

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
    CategoryDetailContent(name = "Ăn uống", getExpensesForCategory = { _, _ -> flowOf(sampleExpenses) })
}
