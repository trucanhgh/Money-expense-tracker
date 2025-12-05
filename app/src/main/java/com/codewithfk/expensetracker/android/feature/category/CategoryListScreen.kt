package com.codewithfk.expensetracker.android.feature.category

import android.net.Uri
import com.codewithfk.expensetracker.android.feature.category.CategoryRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.CategorySummary
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun CategoryListContent(
    getCategoryTotals: (month: String?) -> Flow<List<CategorySummary>>,
    categoriesFlow: Flow<List<CategoryEntity>>,
    onOpenCategory: (name: String) -> Unit,
    onInsertCategory: (name: String) -> Unit,
    onUpdateCategory: (id: Int, newName: String) -> Unit,
    onDeleteCategory: (id: Int) -> Unit
) {
    // category totals (id, name, total)
    val currentMonth = getCurrentMonthString()
    val filterByMonth = remember { mutableStateOf(false) }
    val totalsFlow = getCategoryTotals(if (filterByMonth.value) currentMonth else null)
    val totals by totalsFlow.collectAsState(initial = emptyList())

    // full CategoryEntity list (to get id/name pairs for update/delete)
    val categoryEntities by categoriesFlow.collectAsState(initial = emptyList())

    val showAddDialog = remember { mutableStateOf(false) }
    val newCategoryName = remember { mutableStateOf("") }
    // edit dialog state
    val showEditDialog = remember { mutableStateOf(false) }
    val editCategoryName = remember { mutableStateOf("") }
    val editingCategoryId = remember { mutableStateOf<Int?>(null) }

    // delete confirmation state
    val showDeleteDialog = remember { mutableStateOf(false) }
    val deletingCategoryId = remember { mutableStateOf<Int?>(null) }

    Scaffold(topBar = {}) { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ExpenseTextView(text = "Danh mục", style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { filterByMonth.value = !filterByMonth.value }) {
                            ExpenseTextView(text = if (filterByMonth.value) "Tháng này" else "Tất cả")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        // explicit + button for adding category
                        Button(onClick = { showAddDialog.value = true }) {
                            ExpenseTextView(text = "+ Thêm")
                        }
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                val listToShow: List<CategorySummary> = totals

                LazyColumn {
                    items(items = listToShow, key = { it.id }) { c ->
                        // For each row, pass handlers for edit/delete
                        CategoryRow(c, onClick = {
                            onOpenCategory(c.name)
                        }, onEdit = {
                            editingCategoryId.value = c.id
                            // find name from entities (fallback to summary name)
                            val entity = categoryEntities.find { it.id == c.id }
                            editCategoryName.value = entity?.name ?: c.name
                            showEditDialog.value = true
                        }, onDelete = {
                            // prepare delete confirmation
                            deletingCategoryId.value = c.id
                            showDeleteDialog.value = true
                        })
                    }
                }
            }
        }
    }

    // Add dialog for new category
    if (showAddDialog.value) {
        AlertDialog(
            onDismissRequest = { showAddDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    val name = newCategoryName.value.trim()
                    if (name.isNotEmpty()) {
                        onInsertCategory(name)
                        newCategoryName.value = ""
                        showAddDialog.value = false
                    }
                }) { ExpenseTextView(text = "Lưu") }
            },
            dismissButton = {
                Button(onClick = { showAddDialog.value = false }) { ExpenseTextView(text = "Hủy") }
            },
            text = {
                Column {
                    ExpenseTextView(text = "Thêm danh mục mới", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                    Spacer(modifier = Modifier.size(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = newCategoryName.value,
                        onValueChange = { newCategoryName.value = it },
                        placeholder = { ExpenseTextView(text = "Tên danh mục") }
                    )
                }
            }
        )
    }

    // Edit dialog for rename
    if (showEditDialog.value && editingCategoryId.value != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    val id = editingCategoryId.value!!
                    val newName = editCategoryName.value.trim()
                    if (newName.isNotEmpty()) {
                        onUpdateCategory(id, newName)
                        showEditDialog.value = false
                    }
                }) { ExpenseTextView(text = "Lưu") }
            },
            dismissButton = {
                Button(onClick = { showEditDialog.value = false }) { ExpenseTextView(text = "Hủy") }
            },
            text = {
                Column {
                    ExpenseTextView(text = "Đổi tên danh mục", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                    Spacer(modifier = Modifier.size(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = editCategoryName.value,
                        onValueChange = { editCategoryName.value = it },
                        placeholder = { ExpenseTextView(text = "Tên danh mục") }
                    )
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog.value && deletingCategoryId.value != null) {
        val idToDelete = deletingCategoryId.value!!
        val entityToDelete = categoryEntities.find { it.id == idToDelete }
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false; deletingCategoryId.value = null },
            confirmButton = {
                Button(onClick = {
                    entityToDelete?.let { onDeleteCategory(it.id ?: -1) }
                    showDeleteDialog.value = false
                    deletingCategoryId.value = null
                }) { ExpenseTextView(text = "Xóa") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false; deletingCategoryId.value = null }) { ExpenseTextView(text = "Hủy") }
            },
            text = {
                Column {
                    ExpenseTextView(text = "Xóa danh mục?", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                    Spacer(modifier = Modifier.size(8.dp))
                    ExpenseTextView(text = entityToDelete?.name ?: "")
                }
            }
        )
    }
}

@Composable
fun CategoryListScreen(navController: NavController, viewModel: CategoryViewModel = hiltViewModel()) {
    val totalsFlowProvider: (String?) -> Flow<List<CategorySummary>> = { month -> viewModel.getCategoryTotals(month) }
    val categoriesFlow = viewModel.categories

    CategoryListContent(
        getCategoryTotals = totalsFlowProvider,
        categoriesFlow = categoriesFlow,
        onOpenCategory = { name ->
            val encoded = Uri.encode(name)
            navController.navigate("/category_detail/$encoded")
        },
        onInsertCategory = { name -> viewModel.insertCategory(CategoryEntity(name = name)) },
        onUpdateCategory = { id, newName ->
            // find current entity id/name
            viewModel.updateCategory(CategoryEntity(id = id, name = newName))
        },
        onDeleteCategory = { id ->
            // find entity by id and call viewModel.deleteCategory
            // safe: lookup by id via categoriesFlow not available here synchronously; call delete by constructing entity with id
            viewModel.deleteCategory(CategoryEntity(id = id, name = ""))
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryListContent() {
    val sampleTotals = listOf(
        CategorySummary(id = 1, name = "Ăn uống", total = -120000.0),
        CategorySummary(id = 2, name = "Thu nhập", total = 500000.0)
    )
    val sampleEntities = listOf(
        CategoryEntity(id = 1, name = "Ăn uống"),
        CategoryEntity(id = 2, name = "Thu nhập")
    )

    CategoryListContent(
        getCategoryTotals = { _ -> flowOf(sampleTotals) },
        categoriesFlow = flowOf(sampleEntities),
        onOpenCategory = {},
        onInsertCategory = {},
        onUpdateCategory = { _, _ -> },
        onDeleteCategory = {}
    )
}

fun getCurrentMonthString(): String {
    val cal = Calendar.getInstance()
    val month = cal.get(Calendar.MONTH) + 1
    val year = cal.get(Calendar.YEAR)
    return String.format(Locale.getDefault(), "%02d/%d", month, year)
}
