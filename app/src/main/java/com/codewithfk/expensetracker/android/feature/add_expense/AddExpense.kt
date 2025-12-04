@file:OptIn(ExperimentalMaterial3Api::class)

package com.codewithfk.expensetracker.android.feature.add_expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.base.AddExpenseNavigationEvent
import com.codewithfk.expensetracker.android.base.NavigationEvent
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.ui.theme.InterFontFamily
import com.codewithfk.expensetracker.android.ui.theme.LightGrey
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.ui.theme.Typography
import com.codewithfk.expensetracker.android.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stateless content for AddExpense screen. Accepts providers for categories/goals flows and callbacks.
 */
@Composable
fun AddExpenseContent(
    isIncome: Boolean,
    initialPrefill: String = "",
    categoriesFlowProvider: () -> Flow<List<CategoryEntity>>,
    goalsFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.GoalEntity>>,
    onBack: () -> Unit,
    onMenuClicked: () -> Unit,
    onAddExpenseClick: (ExpenseEntity) -> Unit,
    onInsertCategory: (String, (Boolean) -> Unit) -> Unit
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, card, topBar) = createRefs()
            Image(painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Image(painter = painterResource(id = R.drawable.ic_back), contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onBack() })
                ExpenseTextView(
                    text = "Thêm ${if (isIncome) "thu nhập" else "chi tiêu"}",
                    style = Typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Image(
                        painter = painterResource(id = R.drawable.dots_menu),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { onMenuClicked() }
                    )
                    DropdownMenu(
                        expanded = menuExpanded.value,
                        onDismissRequest = { menuExpanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { ExpenseTextView(text = "Hồ sơ") },
                            onClick = {
                                menuExpanded.value = false
                            }
                        )
                        DropdownMenuItem(
                            text = { ExpenseTextView(text = "Cài đặt") },
                            onClick = {
                                menuExpanded.value = false
                            }
                        )
                    }
                }

            }
            DataForm(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                onAddExpenseClick = onAddExpenseClick,
                isIncome = isIncome,
                categoriesFlow = categoriesFlowProvider(),
                goalsFlow = goalsFlowProvider(),
                initialPrefill = initialPrefill,
                onInsertCategory = onInsertCategory
            )
        }
    }
}

@Composable
fun AddExpense(
    navController: NavController,
    isIncome: Boolean,
    initialPrefill: String = "",
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack()
                AddExpenseNavigationEvent.MenuOpenedClicked -> {
                    // no-op here; UI handles menu locally
                }
                else -> {}
            }
        }
    }

    AddExpenseContent(
        isIncome = isIncome,
        initialPrefill = initialPrefill,
        categoriesFlowProvider = { viewModel.categories },
        goalsFlowProvider = { viewModel.goals },
        onBack = { viewModel.onEvent(AddExpenseUiEvent.OnBackPressed) },
        onMenuClicked = { viewModel.onEvent(AddExpenseUiEvent.OnMenuClicked) },
        onAddExpenseClick = { model -> viewModel.onEvent(AddExpenseUiEvent.OnAddExpenseClicked(model)) },
        onInsertCategory = { name, callback -> viewModel.insertCategory(CategoryEntity(name = name)) ; callback(true) }
    )
}

@Composable
fun DataForm(
    modifier: Modifier,
    onAddExpenseClick: (model: ExpenseEntity) -> Unit,
    isIncome: Boolean,
    categoriesFlow: Flow<List<CategoryEntity>>,
    goalsFlow: Flow<List<com.codewithfk.expensetracker.android.data.model.GoalEntity>>,
    initialPrefill: String = "",
    onInsertCategory: (String, (Boolean) -> Unit) -> Unit
) {

    val name = remember {
        mutableStateOf("")
    }

    // initialize selected if prefill provided
    if (initialPrefill.isNotBlank() && name.value.isBlank()) {
        name.value = initialPrefill
    }

    // missing form state: amount, type, date, dateDialogVisibility
    val amount = remember { mutableStateOf("") }
    val type = remember { mutableStateOf(if (isIncome) "Income" else "Expense") }
    val date = remember { mutableLongStateOf(0L) }
    val dateDialogVisibility = remember { mutableStateOf(false) }

    // collect categories from ViewModel
    val categoriesState by categoriesFlow.collectAsState(initial = emptyList())
    val goalsState by goalsFlow.collectAsState(initial = emptyList())

    // combined list: categories then goals
    val combined = (categoriesState.map { it.name } + goalsState.map { it.name }).distinct()

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitleComponent(title = "Tên danh mục")

        // Category dropdown with add button: use combined list
        CategoryDropdownWithAdd(
            categories = combined,
            onItemSelected = { selected -> name.value = selected },
            onAddCategory = { newName ->
                // insert category via ViewModel
                onInsertCategory(newName) {
                    // select immediately
                    name.value = newName
                }
            }
        )

        Spacer(modifier = Modifier.size(24.dp))
        TitleComponent("Số tiền")
        OutlinedTextField(
            value = amount.value,
            onValueChange = { newValue ->
                amount.value = newValue.filter { it.isDigit() || it == '.' }
            }, textStyle = TextStyle(color = Color.Black),
            visualTransformation = { text ->
                val out = "₫" + text.text
                 val currencyOffsetTranslator = object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int {
                        return offset + 1
                    }

                    override fun transformedToOriginal(offset: Int): Int {
                        return if (offset > 0) offset - 1 else 0
                    }
                }

                TransformedText(AnnotatedString(out), currencyOffsetTranslator)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { ExpenseTextView(text = "Nhập số tiền") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
            )
        )
        Spacer(modifier = Modifier.size(24.dp))
        TitleComponent("Ngày")
        OutlinedTextField(value = if (date.longValue == 0L) "" else Utils.formatDateToHumanReadableForm(
            date.longValue
        ),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dateDialogVisibility.value = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
            ),
            placeholder = { ExpenseTextView(text = "Chọn ngày") })
        Spacer(modifier = Modifier.size(24.dp))
        Button(
            onClick = {
                val trimmedName = name.value.trim()
                val model = ExpenseEntity(
                    null,
                    trimmedName,
                    amount.value.toDoubleOrNull() ?: 0.0,
                    Utils.formatDateToHumanReadableForm(date.longValue),
                    type.value
                )
                onAddExpenseClick(model)
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)
        ) {
            ExpenseTextView(
                text = "Thêm ${if (isIncome) "thu nhập" else "chi tiêu"}",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
    if (dateDialogVisibility.value) {
        ExpenseDatePickerDialog(onDateSelected = {
            date.longValue = it
            dateDialogVisibility.value = false
        }, onDismiss = {
            dateDialogVisibility.value = false
        })
    }
}

@Composable
fun CategoryDropdownWithAdd(
    categories: List<String>,
    onItemSelected: (item: String) -> Unit,
    onAddCategory: (name: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(if (categories.isNotEmpty()) categories[0] else "") }
    var showDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    // keep selected item in sync when categories update
    LaunchedEffect(categories) {
        if (categories.isNotEmpty()) {
            // if previously empty and now has items, pick the last inserted or first
            if (selectedItem.isBlank() || !categories.contains(selectedItem)) {
                selectedItem = categories.last()
                onItemSelected(selectedItem)
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = if (selectedItem.isBlank()) "Chọn hoặc tạo danh mục" else selectedItem,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .menuAnchor(),
                textStyle = TextStyle(fontFamily = InterFontFamily, color = Color.Black),
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                    disabledPlaceholderColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,

                    )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (categories.isEmpty()) {
                    DropdownMenuItem(text = { ExpenseTextView(text = "Chưa có danh mục") }, onClick = { /** no-op */ })
                    DropdownMenuItem(text = { ExpenseTextView(text = "Tạo danh mục mới") }, onClick = {
                        expanded = false
                        showDialog = true
                    })
                } else {
                    categories.forEach {
                        DropdownMenuItem(text = { ExpenseTextView(text = it) }, onClick = {
                            selectedItem = it
                            onItemSelected(selectedItem)
                            expanded = false
                        })
                    }
                    // footer add new
                    DropdownMenuItem(text = { ExpenseTextView(text = "+ Thêm danh mục mới") }, onClick = {
                        expanded = false
                        showDialog = true
                    })
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { showDialog = true }) {
            Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = null)
        }
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false }, confirmButton = {
            TextButton(onClick = {
                if (newCategoryName.isNotBlank()) {
                    val trimmed = newCategoryName.trim()
                    onAddCategory(trimmed)
                    selectedItem = trimmed
                    onItemSelected(trimmed)
                }
                newCategoryName = ""
                showDialog = false
            }) {
                ExpenseTextView(text = "Lưu")
            }
        }, dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                ExpenseTextView(text = "Hủy")
            }
        }, text = {
            Column {
                ExpenseTextView(text = "Thêm danh mục mới", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, placeholder = { ExpenseTextView(text = "Tên danh mục") })
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDatePickerDialog(
    onDateSelected: (date: Long) -> Unit, onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis ?: 0L
    DatePickerDialog(onDismissRequest = { onDismiss() }, confirmButton = {
        TextButton(onClick = { onDateSelected(selectedDate) }) {
            ExpenseTextView(text = "Xác nhận")
        }
    }, dismissButton = {
        TextButton(onClick = { onDateSelected(selectedDate) }) {
            ExpenseTextView(text = "Hủy")
        }
    }) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun TitleComponent(title: String) {
    ExpenseTextView(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = LightGrey
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@Composable
fun ExpenseDropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit) {
    val expanded = remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateOf(listOfItems[0])
    }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            textStyle = TextStyle(fontFamily = InterFontFamily, color = Color.Black),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black, disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

            )
        )
        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { }) {
            listOfItems.forEach {
                DropdownMenuItem(text = { ExpenseTextView(text = it) }, onClick = {
                    selectedItem.value = it
                    onItemSelected(selectedItem.value)
                    expanded.value = false
                })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddExpenseContent() {
    val sampleCategories = listOf(CategoryEntity(id = 1, name = "Ăn uống"), CategoryEntity(id = 2, name = "Di chuyển"))
    val sampleGoals = listOf(com.codewithfk.expensetracker.android.data.model.GoalEntity(id = 1, name = "Du lịch", targetAmount = 5_000_000.0))

    AddExpenseContent(
        isIncome = false,
        initialPrefill = "",
        categoriesFlowProvider = { flowOf(sampleCategories) },
        goalsFlowProvider = { flowOf(sampleGoals) },
        onBack = {},
        onMenuClicked = {},
        onAddExpenseClick = {},
        onInsertCategory = { _, cb -> cb(true) }
    )
}
