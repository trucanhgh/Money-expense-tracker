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
import com.codewithfk.expensetracker.android.widget.TransactionItemRow
import com.codewithfk.expensetracker.android.utils.MoneyFormatting
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import com.codewithfk.expensetracker.android.widget.TopBarWithBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailContent(
    name: String,
    goal: GoalEntity?,
    getContributionsForGoal: (name: String, month: String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    onInsertContribution: (goalName: String, amount: Double, dateStr: String, type: String) -> Unit,
    onOpenTransactions: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    // contributions flow collected inside content per requirement
    val contributionsFlow = getContributionsForGoal(name, null)
    val contributions by contributionsFlow.collectAsState(initial = emptyList())

    // total contributed to this goal: treat Expense as a contribution (money moved into goal) and Income as a withdrawal (money moved out)
    val total = contributions.fold(0.0) { acc, e -> if (e.type == "Expense") acc + e.amount else acc - e.amount }

    // Dialog state for adding contribution
    val showDialog = remember { mutableStateOf(false) }
    val dialogType = remember { mutableStateOf("Expense") } // "Income" or "Expense"
    val amountInput = remember { mutableStateOf("") }
    val dateMillis = remember { mutableStateOf<Long?>(null) }
    val datePickerVisible = remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopBarWithBack(
            title = { ExpenseTextView(text = "Mục tiêu", style = MaterialTheme.typography.titleLarge, color = Color.Black) },
            onBack = onBack
        )
    }) { padding ->
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
                        dateMillis.value = null
                        showDialog.value = true
                    }, modifier = Modifier.weight(1f)) {
                        ExpenseTextView(text = "Rút khỏi quỹ")
                    }

                    Button(onClick = {
                        dialogType.value = "Expense"
                        amountInput.value = ""
                        // keep date unset until user selects
                        dateMillis.value = null
                        showDialog.value = true
                    }, modifier = Modifier.weight(1f)) {
                        ExpenseTextView(text = "Nạp vào quỹ")
                    }
                }

                Spacer(modifier = Modifier.size(16.dp))

                // List of contributions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ExpenseTextView(text = "Giao dịch", color = Color.Black)
                    // Plain clickable text instead of a bordered Button (link style)
                    Text(
                        text = "Xem chi tiết",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable { onOpenTransactions(name) }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))

                if (contributions.isEmpty()) {
                    ExpenseTextView(text = "Không có giao dịch", color = Color.Black)
                } else {
                    // show newest transactions first
                    // Ensure newest transactions appear first: sort by exact creation timestamp then by id as fallback.
                    val sortedContributions = contributions.sortedWith(compareByDescending<com.codewithfk.expensetracker.android.data.model.ExpenseEntity> { it.createdAt }.thenByDescending { it.id ?: 0 })
                    sortedContributions.forEach { e ->
                        // Keep the previous domain-specific sign behavior: contributions (Expense) shown positive,
                        // withdrawals (Income) shown negative. Styling (font, spacing, color) is unified via TransactionItemRow.
                        val amountDisplay = if (e.type == "Expense") Utils.formatCurrency(e.amount) else Utils.formatCurrency(-e.amount)
                        TransactionItemRow(
                            title = e.title,
                            amount = amountDisplay,
                            date = Utils.formatStringDateToMonthDayYear(e.date),
                            isIncome = e.type == "Income",
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }

    // Contribution input dialog
    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, confirmButton = {
            TextButton(onClick = {
                // amountInput stores digits-only (no separators)
                val amt = amountInput.value.toDoubleOrNull() ?: 0.0
                // If no date chosen, fallback to today's local date at start of day in device zone
                val effectiveDate = dateMillis.value ?: LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dateStr = Utils.formatDateToHumanReadableForm(effectiveDate)
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
                OutlinedTextField(
                    value = amountInput.value,
                    onValueChange = { v ->
                        // keep underlying value digits-only; visual transformation shows thousand separators
                        amountInput.value = MoneyFormatting.unformat(v)
                    },
                    visualTransformation = MoneyFormatting.ThousandSeparatorTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { ExpenseTextView(text = "Số tiền (VND)") }
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = dateMillis.value?.let { Utils.formatDateToHumanReadableForm(it) } ?: "",
                    onValueChange = {},
                    modifier = Modifier.clickable { datePickerVisible.value = true },
                    readOnly = true,
                    placeholder = { ExpenseTextView(text = "Chọn ngày") }
                )

                if (datePickerVisible.value) {
                    val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis.value)
                    DatePickerDialog(onDismissRequest = { datePickerVisible.value = false }, confirmButton = {
                        TextButton(onClick = {
                            val selected = pickerState.selectedDateMillis ?: LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            dateMillis.value = selected
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
    navController: NavController,
    encodedName: String?,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val name = encodedName?.let { Uri.decode(it) }?.trim() ?: ""
    val contributionsFlowProvider: (String, String?) -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>> = { n, m -> viewModel.getContributionsForGoal(n, m) }
    val goals by viewModel.goals.collectAsState(initial = emptyList())
    val goal = goals.find { it.name.equals(name, ignoreCase = true) }

    // Provide a top-level Scaffold with a SnackbarHost so ViewModel messages can be shown from here.
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI messages from ViewModel and show them as snackbars
    LaunchedEffect(viewModel) {
        viewModel.uiMessage.collect { msg ->
            // showSnackbar is suspend; ensure sequential display
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            // Forward content to the stateless GoalDetailContent. Keep layout padding.
            GoalDetailContent(
                name = name,
                goal = goal,
                getContributionsForGoal = { n, m -> contributionsFlowProvider(n, m) },
                onInsertContribution = { goalName, amt, dateStr, type ->
                    val normalizedType = when (type) {
                        "Expense", "expense" -> "Expense"
                        "Income", "income" -> "Income"
                        else -> "Expense"
                    }
                    viewModel.insertContribution(goalName, amt, dateStr, normalizedType)
                },
                onOpenTransactions = { goalName ->
                    val encoded = java.net.URLEncoder.encode(goalName, "UTF-8")
                    navController.navigate("/all_transactions/goal/$encoded")
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
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
