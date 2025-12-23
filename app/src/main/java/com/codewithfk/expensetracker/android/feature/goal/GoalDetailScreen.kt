package com.codewithfk.expensetracker.android.feature.goal

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailContent(
    name: String,
    goal: GoalEntity?,
    getContributionsForGoal: (name: String, month: String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    onInsertContribution: (goalName: String, amount: Double, dateStr: String, type: String) -> Unit,
    onOpenTransactions: (String) -> Unit
) {
    // contributions flow collected inside content per requirement
    val contributionsFlow = getContributionsForGoal(name, null)
    val contributions by contributionsFlow.collectAsState(initial = emptyList())

    // total contributed to this goal: treat Expense as a contribution (money moved into goal) and Income as a withdrawal (money moved out)
    // This way when user contributes to a goal we record an Expense (global balance decreases) but goal total increases.
    val total = contributions.fold(0.0) { acc, e -> if (e.type == "Expense") acc + e.amount else acc - e.amount }

    // Dialog state for adding contribution
    val showDialog = remember { mutableStateOf(false) }
    val dialogType = remember { mutableStateOf("Expense") } // "Income" or "Expense"
    val amountInput = remember { mutableStateOf("") }
    // dateMillis == 0L means user hasn't picked a date yet (show empty). If user confirms without selecting,
    // we'll use current time as fallback (consistent with AddExpense behavior).
    val dateMillis = remember { mutableLongStateOf(0L) }
    val datePickerVisible = remember { mutableStateOf(false) }

    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header label (larger & bold) and the actual name (smaller, not bold)
                ExpenseTextView(text = "Mục tiêu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.size(6.dp))
                // Actual goal name: smaller and not bold (single-line with ellipsis for long names)
                ExpenseTextView(text = if (name.isBlank()) "Mục tiêu không hợp lệ" else name, style = MaterialTheme.typography.titleLarge, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.size(12.dp))

                // Goal card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    // Use theme surface so UI follows the app palette instead of a hard-coded color
                    // colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    // Set card background to requested light grey
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // remove "Chủ quỹ" label per requirement
                            // Title already shown at the top of the screen; remove duplicate here
                            Spacer(modifier = Modifier.size(8.dp))

                            val contributedText = Utils.formatCurrency(total)
                            val targetText = Utils.formatCurrency(goal?.targetAmount ?: 0.0)

                            // Make the amount text explicitly black so it contrasts with the light grey card background
                            ExpenseTextView(text = "$contributedText / $targetText", color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                // Buttons: Add Income, Add Outcome
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        dialogType.value = "Income"
                        amountInput.value = ""
                        // don't prefill date; keep 0L so field stays empty unless user picks a date
                        dateMillis.longValue = 0L
                        showDialog.value = true
                    }, modifier = Modifier.weight(1f)) {
                        ExpenseTextView(text = "Rút khỏi quỹ")
                    }

                    Button(onClick = {
                        dialogType.value = "Expense"
                        amountInput.value = ""
                        // keep date unset until user selects
                        dateMillis.longValue = 0L
                        showDialog.value = true
                    }, modifier = Modifier.weight(1f)) {
                        ExpenseTextView(text = "Nạp vào quỹ")
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                // List of contributions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ExpenseTextView(text = "Giao dịch", color = Color.Black)
                    Button(onClick = { onOpenTransactions(name) }) {
                        ExpenseTextView(text = "Xem chi tiết")
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))

                if (contributions.isEmpty()) {
                    ExpenseTextView(text = "Không có giao dịch", color = Color.Black)
                } else {
                    // show newest transactions first
                    val sortedContributions = contributions.sortedByDescending { Utils.getMillisFromDate(it.date) }
                    sortedContributions.forEach { e ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            ExpenseTextView(text = e.title, color = Color.Black)
                            // Show positive amount for contributions (Expense), negative for withdrawals (Income)
                            val amountDisplay = if (e.type == "Expense") Utils.formatCurrency(e.amount) else Utils.formatCurrency(-e.amount)
                            ExpenseTextView(text = amountDisplay)
                        }
                    }
                }
            }
        }
    }

    // Contribution input dialog
    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, confirmButton = {
            TextButton(onClick = {
                // normalize thousand separators and commas
                val cleaned = amountInput.value.replace(Regex("[.,\\s]"), "")
                val amt = cleaned.toDoubleOrNull() ?: 0.0
                val dateStr = Utils.formatDateToHumanReadableForm(dateMillis.longValue)
                // insert via callback
                onInsertContribution(name, amt, dateStr, dialogType.value)
                showDialog.value = false
            }) { ExpenseTextView(text = "Lưu") }
        }, dismissButton = {
            TextButton(onClick = { showDialog.value = false }) { ExpenseTextView(text = "Hủy") }
        }, text = {
            Column {
                // Clarify semantics: Deposit into goal should be an Expense (reduces global balance),
                // Withdraw from goal should be an Income (increases global balance).
                ExpenseTextView(text = if (dialogType.value == "Income") "Rút khỏi mục tiêu" else "Nạp vào mục tiêu", color = Color.Black)
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(value = amountInput.value, onValueChange = { v ->
                    // allow digits and dots and commas
                    amountInput.value = v.filter { it.isDigit() || it == '.' || it == ',' }
                }, placeholder = { ExpenseTextView(text = "Số tiền (VND)") })
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(value = Utils.formatDateToHumanReadableForm(dateMillis.longValue), onValueChange = {}, modifier = Modifier.clickable { datePickerVisible.value = true }, readOnly = true, placeholder = { ExpenseTextView(text = "Chọn ngày") })

                if (datePickerVisible.value) {
                    val pickerState = rememberDatePickerState()
                    DatePickerDialog(onDismissRequest = { datePickerVisible.value = false }, confirmButton = {
                        TextButton(onClick = {
                            val selected = pickerState.selectedDateMillis ?: System.currentTimeMillis()
                            dateMillis.longValue = selected
                            datePickerVisible.value = false
                        }) { ExpenseTextView(text = "Xác nhận") }
                    }, dismissButton = {
                        TextButton(onClick = { datePickerVisible.value = false }) { ExpenseTextView(text = "Hủy") }
                    }) {
                        DatePicker(state = pickerState)
                    }
                }
            }
        })
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun GoalDetailScreen(
    _navController: NavController,
    encodedName: String?,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val name = encodedName?.let { Uri.decode(it) }?.trim() ?: ""
    val contributionsFlowProvider: (String, String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>> = { n, m -> viewModel.getContributionsForGoal(n, m) }
    val goals by viewModel.goals.collectAsState(initial = emptyList())
    val goal = goals.find { it.name.equals(name, ignoreCase = true) }

    GoalDetailContent(
        name = name,
        goal = goal,
        getContributionsForGoal = { n, m -> contributionsFlowProvider(n, m) },
        onInsertContribution = { goalName, amt, dateStr, type ->
            // Ensure deposit => Expense and withdrawal => Income.
            // If UI mistakenly passes a dialog type opposite to semantics, normalize here.
            val normalizedType = when (type) {
                "Expense", "expense" -> "Expense"
                "Income", "income" -> "Income"
                else -> "Expense"
            }
            viewModel.insertContribution(goalName, amt, dateStr, normalizedType)
        },
        onOpenTransactions = { goalName ->
            val encoded = java.net.URLEncoder.encode(goalName, "UTF-8")
            _navController.navigate("/all_transactions/goal/$encoded")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewGoalDetailContent() {
    val sampleContributions = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 1, title = "Đóng góp 1", amount = 200000.0, date = "01/12/2025", type = "Expense"),
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 2, title = "Rút tiền", amount = 50000.0, date = "02/12/2025", type = "Income")
    )
    val sampleGoal = GoalEntity(id = 1, name = "Du lịch", targetAmount = 5_000_000.0)
    ExpenseTrackerAndroidTheme {
        GoalDetailContent(
            name = "Du lịch",
            goal = sampleGoal,
            getContributionsForGoal = { _, _ -> flowOf(sampleContributions) },
            onInsertContribution = { _, _, _, _ -> },
            onOpenTransactions = {}
        )
    }
}
