package com.codewithfk.expensetracker.android.feature.category

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.CategorySummary
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.widget.TopBarWithBack
import kotlinx.coroutines.flow.Flow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar
import java.util.Locale
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.codewithfk.expensetracker.android.utils.MoneyFormatting

@Composable
fun CategoryListContent(
    getCategoryTotals: (month: String?) -> Flow<List<CategorySummary>>,
    categoriesFlow: Flow<List<CategoryEntity>>,
    onOpenCategory: (name: String) -> Unit,
    onInsertCategory: (CategoryEntity) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (id: Int) -> Unit,
    onBack: () -> Unit = {}
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
    val showFilterDialog = remember { mutableStateOf(false) }

    // auto transaction states for add dialog
    val addAutoEnabled = remember { mutableStateOf(false) }
    val addAutoAmount = remember { mutableStateOf(0.0) }
    // string backing for input (digits only). VisualTransformation will show dots.
    val addAutoAmountInput = remember { mutableStateOf("") }
    val addAutoType = remember { mutableStateOf("Expense") }
    val addAutoRepeatType = remember { mutableStateOf("WEEKLY") }
    val addAutoDayOfWeek = remember { mutableStateOf<Int?>(null) }
    val addAutoDayOfMonth = remember { mutableStateOf<Int?>(null) }

    // scroll state for add/edit weekday rows (keep at top-level @Composable scope)
    val addScrollState = rememberScrollState()
    val editScrollState = rememberScrollState()

    // edit dialog state
    val showEditDialog = remember { mutableStateOf(false) }
    val editCategoryName = remember { mutableStateOf("") }
    val editingCategoryId = remember { mutableStateOf<Int?>(null) }

    // auto transaction states for edit dialog
    val editAutoEnabled = remember { mutableStateOf(false) }
    val editAutoAmount = remember { mutableStateOf(0.0) }
    val editAutoAmountInput = remember { mutableStateOf("") }
    val editAutoType = remember { mutableStateOf("Expense") }
    val editAutoRepeatType = remember { mutableStateOf("WEEKLY") }
    val editAutoDayOfWeek = remember { mutableStateOf<Int?>(null) }
    val editAutoDayOfMonth = remember { mutableStateOf<Int?>(null) }

    // delete confirmation state
    val showDeleteDialog = remember { mutableStateOf(false) }
    val deletingCategoryId = remember { mutableStateOf<Int?>(null) }

    Scaffold() { padding ->
        Surface(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Move TopBarWithBack here so it aligns vertically the same as StatsScreen
                TopBarWithBack(
                    title = { ExpenseTextView(text = "Danh mục", style = MaterialTheme.typography.titleLarge, color = Color.Black) },
                    onBack = onBack
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Title shown in topBar; keep placeholder and + button on the right
                    ExpenseTextView(text = "", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                            // populate auto fields from entity
                            editAutoEnabled.value = entity?.isAutoTransactionEnabled ?: false
                            editAutoAmount.value = entity?.autoAmount ?: 0.0
                            editAutoType.value = entity?.autoType ?: "Expense"
                            editAutoRepeatType.value = entity?.autoRepeatType ?: "WEEKLY"
                            editAutoDayOfWeek.value = entity?.autoDayOfWeek
                            editAutoDayOfMonth.value = entity?.autoDayOfMonth

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
                        val category = CategoryEntity(
                            name = name,
                            isAutoTransactionEnabled = addAutoEnabled.value,
                            autoAmount = addAutoAmount.value,
                            autoType = addAutoType.value,
                            autoRepeatType = addAutoRepeatType.value,
                            autoDayOfWeek = addAutoDayOfWeek.value,
                            autoDayOfMonth = addAutoDayOfMonth.value
                        )
                        onInsertCategory(category)
                        newCategoryName.value = ""
                        // reset add states
                        addAutoEnabled.value = false
                        addAutoAmount.value = 0.0
                        addAutoType.value = "Expense"
                        addAutoRepeatType.value = "WEEKLY"
                        addAutoDayOfWeek.value = null
                        addAutoDayOfMonth.value = null
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

                    Spacer(modifier = Modifier.size(12.dp))

                    // Auto transaction section
                    // Compact auto-transaction toggle (no headline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Switch(checked = addAutoEnabled.value, onCheckedChange = { addAutoEnabled.value = it })
                        Spacer(modifier = Modifier.size(8.dp))
                        ExpenseTextView(text = "Bật chi tiêu tự động")
                    }

                    if (addAutoEnabled.value) {
                        Spacer(modifier = Modifier.size(8.dp))
                        // Amount
                        androidx.compose.material3.OutlinedTextField(
                            value = addAutoAmountInput.value,
                            onValueChange = { v ->
                                // keep only digits
                                val digitsOnly = MoneyFormatting.unformat(v)
                                addAutoAmountInput.value = digitsOnly
                                addAutoAmount.value = digitsOnly.toDoubleOrNull() ?: 0.0
                            },
                            visualTransformation = MoneyFormatting.ThousandSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { ExpenseTextView(text = "Số tiền") }
                        )

                        Spacer(modifier = Modifier.size(8.dp))
                        // Type: Expense or Income
                        Row {
                            androidx.compose.material3.RadioButton(selected = addAutoType.value == "Expense", onClick = { addAutoType.value = "Expense" })
                            ExpenseTextView(text = "Chi")
                            Spacer(modifier = Modifier.size(12.dp))
                            androidx.compose.material3.RadioButton(selected = addAutoType.value == "Income", onClick = { addAutoType.value = "Income" })
                            ExpenseTextView(text = "Thu")
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        // Repeat type dropdown (simple toggles)
                        Row {
                            Button(onClick = { addAutoRepeatType.value = "WEEKLY" }) { ExpenseTextView(text = if (addAutoRepeatType.value == "WEEKLY") "Hằng tuần ✓" else "Hằng tuần") }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(onClick = { addAutoRepeatType.value = "MONTHLY" }) { ExpenseTextView(text = if (addAutoRepeatType.value == "MONTHLY") "Hằng tháng ✓" else "Hằng tháng") }
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        if (addAutoRepeatType.value == "WEEKLY") {
                            // Compact single-row weekday selector: S M T W T F S
                            val display = listOf("S", "M", "T", "W", "T", "F", "S")
                            // Map displayed positions to stored day values (1=Monday..7=Sunday)
                            val dayMap = listOf(7, 1, 2, 3, 4, 5, 6)
                            Row(modifier = Modifier
                                .horizontalScroll(addScrollState)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                display.forEachIndexed { idx, ch ->
                                    val dayValue = dayMap[idx]
                                    val selected = addAutoDayOfWeek.value == dayValue
                                    WeekdayChip(
                                        label = ch,
                                        selected = selected,
                                        onClick = { addAutoDayOfWeek.value = if (selected) null else dayValue },
                                        size = 34.dp
                                    )
                                }
                            }
                        } else {
                            // Monthly: day of month input (1-28)
                            androidx.compose.material3.OutlinedTextField(
                                value = addAutoDayOfMonth.value?.toString() ?: "",
                                onValueChange = { v ->
                                    val cleaned = v.filter { it.isDigit() }
                                    val iv = cleaned.toIntOrNull()
                                    if (iv != null && iv in 1..28) addAutoDayOfMonth.value = iv
                                },
                                placeholder = { ExpenseTextView(text = "Ngày trong tháng (1-28)") }
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        ExpenseTextView(text = "Giao dịch sẽ tự động tạo theo lịch. Bạn vẫn có thể nhập giao dịch thủ công.")
                    }
                }
            }
        )
    }

    // Edit dialog for rename and auto config
    if (showEditDialog.value && editingCategoryId.value != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    val id = editingCategoryId.value!!
                    val newName = editCategoryName.value.trim()
                    if (newName.isNotEmpty()) {
                        val updated = CategoryEntity(
                            id = id,
                            name = newName,
                            isAutoTransactionEnabled = editAutoEnabled.value,
                            autoAmount = editAutoAmount.value,
                            autoType = editAutoType.value,
                            autoRepeatType = editAutoRepeatType.value,
                            autoDayOfWeek = editAutoDayOfWeek.value,
                            autoDayOfMonth = editAutoDayOfMonth.value
                        )
                        onUpdateCategory(updated)
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

                    Spacer(modifier = Modifier.size(12.dp))

                    // Auto transaction section (same as Add)
                    // Compact auto-transaction toggle (no headline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Switch(checked = editAutoEnabled.value, onCheckedChange = { editAutoEnabled.value = it })
                        Spacer(modifier = Modifier.size(8.dp))
                        ExpenseTextView(text = "Bật chi tiêu tự động")
                    }

                    if (editAutoEnabled.value) {
                        Spacer(modifier = Modifier.size(8.dp))
                        // Amount
                        androidx.compose.material3.OutlinedTextField(
                            value = editAutoAmountInput.value,
                            onValueChange = { v ->
                                val digitsOnly = MoneyFormatting.unformat(v)
                                editAutoAmountInput.value = digitsOnly
                                editAutoAmount.value = digitsOnly.toDoubleOrNull() ?: 0.0
                            },
                            visualTransformation = MoneyFormatting.ThousandSeparatorTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { ExpenseTextView(text = "Số tiền") }
                        )

                        Spacer(modifier = Modifier.size(8.dp))
                        // Type
                        Row {
                            androidx.compose.material3.RadioButton(selected = editAutoType.value == "Expense", onClick = { editAutoType.value = "Expense" })
                            ExpenseTextView(text = "Chi")
                            Spacer(modifier = Modifier.size(12.dp))
                            androidx.compose.material3.RadioButton(selected = editAutoType.value == "Income", onClick = { editAutoType.value = "Income" })
                            ExpenseTextView(text = "Thu")
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        // Repeat type
                        Row {
                            Button(onClick = { editAutoRepeatType.value = "WEEKLY" }) { ExpenseTextView(text = if (editAutoRepeatType.value == "WEEKLY") "Hằng tuần ✓" else "Hằng tuần") }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(onClick = { editAutoRepeatType.value = "MONTHLY" }) { ExpenseTextView(text = if (editAutoRepeatType.value == "MONTHLY") "Hằng tháng ✓" else "Hằng tháng") }
                        }

                        Spacer(modifier = Modifier.size(8.dp))
                        if (editAutoRepeatType.value == "WEEKLY") {
                            // Compact single-row weekday selector: S M T W T F S
                            val display = listOf("S", "M", "T", "W", "T", "F", "S")
                            val dayMap = listOf(7, 1, 2, 3, 4, 5, 6)
                            Row(modifier = Modifier
                                .horizontalScroll(editScrollState)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                display.forEachIndexed { idx, ch ->
                                    val dayValue = dayMap[idx]
                                    val selected = editAutoDayOfWeek.value == dayValue
                                    WeekdayChip(
                                        label = ch,
                                        selected = selected,
                                        onClick = { editAutoDayOfWeek.value = if (selected) null else dayValue },
                                        size = 34.dp
                                    )
                                }
                            }
                        } else {
                             androidx.compose.material3.OutlinedTextField(
                                 value = editAutoDayOfMonth.value?.toString() ?: "",
                                 onValueChange = { v ->
                                     val cleaned = v.filter { it.isDigit() }
                                     val iv = cleaned.toIntOrNull()
                                     if (iv != null && iv in 1..28) editAutoDayOfMonth.value = iv
                                 },
                                 placeholder = { ExpenseTextView(text = "Ngày trong tháng (1-28)") }
                             )
                         }

                        Spacer(modifier = Modifier.size(8.dp))
                        ExpenseTextView(text = "Giao dịch sẽ tự động tạo theo lịch. Bạn vẫn có thể nhập giao dịch thủ công.")
                     }
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

    // Filter dialog: choose between showing all or this month
    if (showFilterDialog.value) {
        AlertDialog(onDismissRequest = { showFilterDialog.value = false }, confirmButton = {
            Button(onClick = { showFilterDialog.value = false }) { ExpenseTextView(text = "Đóng") }
        }, text = {
            Column {
                ExpenseTextView(text = "Lọc giao dịch", fontWeight = MaterialTheme.typography.titleLarge.fontWeight)
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = {
                    filterByMonth.value = false
                    showFilterDialog.value = false
                }) { ExpenseTextView(text = "Hiển thị tất cả") }
                Spacer(modifier = Modifier.size(8.dp))
                Button(onClick = {
                    filterByMonth.value = true
                    showFilterDialog.value = false
                }) { ExpenseTextView(text = "Tháng này") }
            }
        })
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
            // Navigate directly to the shared Transaction screen with a category filter
            navController.navigate("/all_transactions/category/$encoded")
         },
         onInsertCategory = { category -> viewModel.insertCategory(category) },
         onUpdateCategory = { category -> viewModel.updateCategory(category) },
         onDeleteCategory = { id ->
             // find entity by id and call viewModel.deleteCategory
             // safe: lookup by id via categoriesFlow not available here synchronously; call delete by constructing entity with id
             viewModel.deleteCategory(CategoryEntity(id = id, name = ""))
         },
        onBack = { navController.popBackStack() }
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

     ExpenseTrackerAndroidTheme {
         CategoryListContent(
             getCategoryTotals = { _ -> flowOf(sampleTotals) },
             categoriesFlow = flowOf(sampleEntities),
             onOpenCategory = {},
             onInsertCategory = {},
             onUpdateCategory = { },
             onDeleteCategory = {}
         )
     }
 }

 // WeekdayChip implementation (kept at bottom of file or near other helpers)
 @Composable
 private fun WeekdayChip(
     label: String,
     selected: Boolean,
     onClick: () -> Unit,
     size: Dp = 34.dp,
     shape: Shape = RoundedCornerShape(6.dp),
     textSize: TextUnit = 12.sp
 ) {
     val bgColor = if (selected) Color(0xFFE0E0E0) else Color.Transparent
     val borderColor = if (selected) Color.Transparent else Color(0xFFBDBDBD)

     Box(
         modifier = Modifier
             .size(size)
             .clip(shape)
             .background(bgColor)
             .then(if (!selected) Modifier.border(width = 1.dp, color = borderColor, shape = shape) else Modifier)
             .clickable(onClick = onClick),
         contentAlignment = Alignment.Center
     ) {
         Text(
             text = label,
             fontSize = textSize,
             color = Color.Black,
             textAlign = TextAlign.Center,
             fontWeight = FontWeight.Medium
         )
     }
 }

 fun getCurrentMonthString(): String {
     val cal = Calendar.getInstance()
     val month = cal.get(Calendar.MONTH) + 1
     val year = cal.get(Calendar.YEAR)
     return String.format(Locale.getDefault(), "%02d/%d", month, year)
 }
