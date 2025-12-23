package com.codewithfk.expensetracker.android.feature.transactionlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.feature.add_expense.ExpenseDropDown
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.widget.TransactionItemRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Button

@Composable
fun TransactionListContent(
    expensesFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    onBack: () -> Unit
) {
    // For backward compatibility - not used after refactor
    val state by expensesFlowProvider().collectAsState(initial = emptyList())
    TransactionListContentBase(expensesFlow = { flowOf(state) }, onBack = onBack)
}

@Composable
fun TransactionListScreen(navController: NavController, categoryName: String? = null, goalName: String? = null, viewModel: TransactionViewModel = hiltViewModel()) {
    // Configure ViewModel params
    LaunchedEffect(categoryName, goalName) {
        viewModel.setParams(categoryName, goalName)
    }

    val expenses by viewModel.filteredExpenses.collectAsState()

    TransactionListContentBase(expensesFlow = { viewModel.filteredExpenses }, onBack = { navController.popBackStack() }, viewModel = viewModel)
}

@Composable
fun TransactionListContentBase(
    expensesFlow: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    onBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val expenses by expensesFlow().collectAsState(initial = emptyList())
    var showFilter by remember { mutableStateOf(false) }

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

                // Filter icon on the right
                Row(modifier = Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Bộ lọc")
                    }
                }
            }
        }
    ) { paddingValues ->
        // Apply the Scaffold content padding to the root Box so both the list and
        // the overlay are positioned below the TopAppBar. The overlay will be
        // aligned to the top of this content area so it appears directly under
        // the header and covers the list instead of pushing it.
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // Transaction list (full screen under the overlay). Don't apply the
            // Scaffold padding again to the LazyColumn to avoid double-padding.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(expenses) { item ->
                    val amount = if (item.type == "Income") item.amount else -item.amount
                    TransactionItemRow(
                        title = item.title,
                        amount = Utils.formatCurrency(amount),
                        date = Utils.formatStringDateToMonthDayYear(item.date),
                        isIncome = item.type == "Income",
                        modifier = Modifier
                    )
                }
            }

            // Overlay filter panel at the top - does not push content
            AnimatedVisibility(visible = showFilter, enter = fadeIn(), exit = fadeOut()) {
                // Align the filter surface to the top of the content area so it appears
                // directly below the TopAppBar and overlays the transaction list.
                Surface(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopStart), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Type Filter
                        ExpenseDropDown(
                            listOfItems = listOf("Tất cả", "Chi tiêu", "Thu nhập"),
                            onItemSelected = { selected ->
                                viewModel.setFilterType(selected)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Date Range Filter - map choices to DateRange
                        ExpenseDropDown(
                            listOfItems = listOf("Tất cả", "Hôm nay", "Hôm qua", "30 ngày qua", "90 ngày qua", "1 năm"),
                            onItemSelected = { selected ->
                                when (selected) {
                                    "Tất cả" -> viewModel.setDateRange(DateRange.ALL)
                                    "Hôm nay" -> viewModel.setDateRange(DateRange.TODAY)
                                    "Hôm qua" -> viewModel.setDateRange(DateRange.YESTERDAY)
                                    "30 ngày qua" -> viewModel.setDateRange(DateRange.LAST_30_DAYS)
                                    "90 ngày qua" -> viewModel.setDateRange(DateRange.LAST_90_DAYS)
                                    "1 năm" -> viewModel.setDateRange(DateRange.LAST_365_DAYS)
                                    else -> viewModel.setDateRange(DateRange.ALL)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.resetFilters() }) {
                                ExpenseTextView(text = "Đặt lại")
                            }
                            Button(onClick = { showFilter = false }) {
                                ExpenseTextView(text = "Áp dụng")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTransactionListContent() {
    val sample = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 1, title = "Ăn uống", amount = 120000.0, date = "01/12/2025", type = "Expense"),
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 2, title = "Lương", amount = 5000000.0, date = "01/12/2025", type = "Income")
    )
    ExpenseTrackerAndroidTheme {
        TransactionListContentBase(expensesFlow = { flowOf(sample) }, onBack = {})
    }
}
