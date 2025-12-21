package com.codewithfk.expensetracker.android.feature.transactionlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.feature.add_expense.ExpenseDropDown
import com.codewithfk.expensetracker.android.feature.home.TransactionItem
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.feature.home.HomeViewModel
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListContent(
    expensesFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    onBack: () -> Unit
) {
    val state by expensesFlowProvider().collectAsState(initial = emptyList())
    var filterType by remember { mutableStateOf("Tất cả") }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredTransactions = when (filterType) {
        "Chi tiêu" -> state.filter { it.type == "Expense" }
        "Thu nhập" -> state.filter { it.type == "Income" }
        else -> state
    }

    val filteredByDateRange = filteredTransactions.filter { transaction ->
        // no-op: keep existing behaviour
        true
    }

    // Sort by date descending (newest first). Dates are stored as String dd/MM/yyyy; convert to millis for sorting.
    val sortedTransactions = filteredByDateRange.sortedByDescending { Utils.getMillisFromDate(it.date) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                // Back Button
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Quay lại",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onBack() },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )

                // Title
                ExpenseTextView(
                    text = "Giao dịch",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )

                // Three Dots Menu
                Image(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { menuExpanded = !menuExpanded },
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content area for the transaction list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Dropdowns
                    AnimatedVisibility(
                        visible = menuExpanded,
                        enter = slideInVertically(initialOffsetY = { -it / 2 }),
                        exit = slideOutVertically(targetOffsetY = { -it  }),
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        // Use a Surface to make the panel span full width and use app surface color (usually white)
                        androidx.compose.material3.Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Type Filter Dropdown
                                ExpenseDropDown(
                                    listOfItems = listOf("Tất cả", "Chi tiêu", "Thu nhập"),
                                    onItemSelected = { selected ->
                                        filterType = selected
                                        menuExpanded = false // Close menu after selection
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Date Range Filter Dropdown
                                ExpenseDropDown(
                                    listOfItems = listOf( "Hôm qua", "Hôm nay", "30 ngày qua", "90 ngày qua", "1 năm"),
                                    onItemSelected = { selected ->
                                        // date range selection currently not used
                                        menuExpanded = false // Close menu after selection
                                    }
                                )
                            }
                        }
                    }
                }
                items(sortedTransactions) { item ->
                    val amount = if (item.type == "Income") item.amount else -item.amount
                    TransactionItem(
                        title = item.title,
                        amount = Utils.formatCurrency(amount),
                        date = Utils.formatStringDateToMonthDayYear(item.date),
                        color = if (item.type == "Income") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        Modifier
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val expensesFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>> = { viewModel.expenses }

    TransactionListContent(
        expensesFlowProvider = expensesFlowProvider,
        onBack = { navController.popBackStack() }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTransactionListContent() {
    val sample = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 1, title = "Ăn uống", amount = 120000.0, date = "01/12/2025", type = "Expense"),
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 2, title = "Lương", amount = 5000000.0, date = "01/12/2025", type = "Income")
    )
    ExpenseTrackerAndroidTheme {
        TransactionListContent(
            expensesFlowProvider = { flowOf(sample) },
            onBack = {}
        )
    }
}
