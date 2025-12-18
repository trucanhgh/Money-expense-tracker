package com.codewithfk.expensetracker.android.feature.goal

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

/**
 * Stateless UI for Goal List. Accepts data and callbacks. It may call getContributionsForGoal to collect flows.
 */
@Composable
fun GoalListContent(
    goals: List<GoalEntity>,
    getContributionsForGoal: (goalName: String, month: String?) -> Flow<List<ExpenseEntity>>,
    onCreateGoal: (name: String, target: Double) -> Unit,
    onOpenGoal: (name: String) -> Unit,
    onUpdateGoal: (goal: GoalEntity) -> Unit,
    onDeleteGoal: (goal: GoalEntity) -> Unit
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val newName = remember { mutableStateOf("") }
    val newTarget = remember { mutableStateOf("") }

    // edit / delete dialog state
    val showEditDialog = remember { mutableStateOf(false) }
    val editGoalName = remember { mutableStateOf("") }
    val editingGoalId = remember { mutableStateOf<Int?>(null) }

    val showDeleteDialog = remember { mutableStateOf(false) }
    val deletingGoalId = remember { mutableStateOf<Int?>(null) }

    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ExpenseTextView(text = "Quỹ của tôi (${goals.size})", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = { showAddDialog.value = true }) {
                        ExpenseTextView(text = "+ Thêm")
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                LazyColumn {
                    items(goals, key = { it.id ?: 0 }) { g ->
                        val contributionsForG by getContributionsForGoal(g.name, null).collectAsState(initial = emptyList())
                        // Treat Expense as contribution (money moved into the goal) and Income as withdrawal
                        // so that goal total = sum(Expense) - sum(Income)
                        val contributedTotal = contributionsForG.fold(0.0) { acc, e -> if (e.type == "Expense") acc + e.amount else acc - e.amount }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            // Use a column to place title at top, then the progress pill one row lower
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenGoal(g.name)
                                }
                                .padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    // Top-left: goal name
                                    ExpenseTextView(text = g.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)

                                    // Top-right: overflow menu ("...")
                                    var menuExpanded by remember { mutableStateOf(false) }
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(painter = painterResource(id = R.drawable.dots_menu) , contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                        DropdownMenuItem(text = { Text("Đổi tên") }, onClick = {
                                            menuExpanded = false
                                            editingGoalId.value = g.id
                                            editGoalName.value = g.name
                                            showEditDialog.value = true
                                        })
                                        DropdownMenuItem(text = { Text("Xóa") }, onClick = {
                                            menuExpanded = false
                                            deletingGoalId.value = g.id
                                            showDeleteDialog.value = true
                                        })
                                    }
                                }

                                Spacer(modifier = Modifier.size(12.dp))

                                // Money/progress pill on its own row below the title
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                                    Card(
                                        shape = RoundedCornerShape(999.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(painter = painterResource(id = R.drawable.ic_income), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.size(8.dp))
                                            val contributedText = Utils.formatCurrency(contributedTotal)
                                            val targetText = Utils.formatCurrency(g.targetAmount)
                                            // Put amount on a separate line if needed by using Text with maxLines=2
                                            ExpenseTextView(text = "$contributedText / $targetText", color = MaterialTheme.colorScheme.onSecondary, maxLines = 2)
                                        }
                                    }
                                }

                                // Optionally other content (like progress bar) could go here
                            }
                        }
                    }
                }
            }
        }
    }

    // Add dialog for new goal
    if (showAddDialog.value) {
        AlertDialog(onDismissRequest = { showAddDialog.value = false }, confirmButton = {
            Button(onClick = {
                val name = newName.value.trim()
                val target = newTarget.value.replace(Regex("[.,\\s]"), "").toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) {
                    onCreateGoal(name, target)
                    newName.value = ""
                    newTarget.value = ""
                    showAddDialog.value = false
                }
            }) { ExpenseTextView(text = "Lưu") }
        }, dismissButton = {
            Button(onClick = { showAddDialog.value = false }) { ExpenseTextView(text = "Hủy") }
        }, text = {
            Column {
                ExpenseTextView(text = "Tạo mục tiêu mới")
                Spacer(modifier = Modifier.size(8.dp))
                androidx.compose.material3.OutlinedTextField(value = newName.value, onValueChange = { newName.value = it }, placeholder = { ExpenseTextView(text = "Tên mục tiêu") })
                Spacer(modifier = Modifier.size(8.dp))
                androidx.compose.material3.OutlinedTextField(value = newTarget.value, onValueChange = { newTarget.value = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } }, placeholder = { ExpenseTextView(text = "Số tiền mục tiêu (VND)") })
            }
        })
    }

    // Edit dialog for renaming
    if (showEditDialog.value && editingGoalId.value != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    val id = editingGoalId.value!!
                    val newNameTrimmed = editGoalName.value.trim()
                    if (newNameTrimmed.isNotEmpty()) {
                        onUpdateGoal(GoalEntity(id = id, name = newNameTrimmed, targetAmount = goals.find { it.id == id }?.targetAmount ?: 0.0))
                        showEditDialog.value = false
                    }
                }) { ExpenseTextView(text = "Lưu") }
            },
            dismissButton = { Button(onClick = { showEditDialog.value = false }) { ExpenseTextView(text = "Hủy") } },
            text = {
                Column {
                    ExpenseTextView(text = "Đổi tên mục tiêu")
                    Spacer(modifier = Modifier.size(8.dp))
                    androidx.compose.material3.OutlinedTextField(value = editGoalName.value, onValueChange = { editGoalName.value = it }, placeholder = { ExpenseTextView(text = "Tên mục tiêu") })
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog.value && deletingGoalId.value != null) {
        val idToDelete = deletingGoalId.value!!
        val entityToDelete = goals.find { it.id == idToDelete }
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false; deletingGoalId.value = null },
            confirmButton = {
                Button(onClick = {
                    entityToDelete?.let { onDeleteGoal(it) }
                    showDeleteDialog.value = false
                    deletingGoalId.value = null
                }) { ExpenseTextView(text = "Xóa") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false; deletingGoalId.value = null }) { ExpenseTextView(text = "Hủy") }
            },
            text = {
                Column {
                    ExpenseTextView(text = "Xóa mục tiêu?", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                    Spacer(modifier = Modifier.size(8.dp))
                    ExpenseTextView(text = entityToDelete?.name ?: "")
                }
            }
        )
    }
}

/**
 * Wrapper Screen that uses ViewModel and NavController and passes data/callbacks to GoalListContent
 */
@Composable
fun GoalListScreen(navController: NavController, viewModel: GoalViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsState(initial = emptyList())

    GoalListContent(
        goals = goals,
        getContributionsForGoal = { name, month -> viewModel.getContributionsForGoal(name, month) },
        onCreateGoal = { name, target -> viewModel.insertGoal(GoalEntity(name = name, targetAmount = target)) },
        onOpenGoal = { name ->
            val encoded = Uri.encode(name)
            navController.navigate("/goal_detail/$encoded")
        },
        onUpdateGoal = { goal -> viewModel.updateGoal(goal) },
        onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
    )
}

/**
 * Preview with fake data. Use safe fallbacks for resources.
 */
@Preview(showBackground = true)
@Composable
fun PreviewGoalListContent() {
    val sample = listOf(
        GoalEntity(id = 1, name = "Du lịch", targetAmount = 5_000_000.0),
        GoalEntity(id = 2, name = "Xe máy", targetAmount = 20_000_000.0)
    )
    GoalListContent(
        goals = sample,
        getContributionsForGoal = { _, _ -> flowOf(emptyList()) },
        onCreateGoal = { _, _ -> },
        onOpenGoal = {},
        onUpdateGoal = {},
        onDeleteGoal = {}
    )
}
