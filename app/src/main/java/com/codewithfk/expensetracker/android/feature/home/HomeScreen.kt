package com.codewithfk.expensetracker.android.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.base.HomeNavigationEvent
import com.codewithfk.expensetracker.android.base.NavigationEvent
import com.codewithfk.expensetracker.android.ui.theme.Red
import com.codewithfk.expensetracker.android.ui.theme.Typography
import com.codewithfk.expensetracker.android.ui.theme.LocalAppUi
import com.codewithfk.expensetracker.android.ui.theme.ExpenseTrackerAndroidTheme
import com.codewithfk.expensetracker.android.utils.Utils
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon as MIcon
import com.codewithfk.expensetracker.android.feature.notification.NotificationViewModel
import com.codewithfk.expensetracker.android.data.model.NotificationEntity

@Composable
fun HomeContent(
    expenses: List<ExpenseEntity>,
    onSeeAllClicked: () -> Unit,
    onAddExpenseClicked: () -> Unit,
    onAddIncomeClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    val appUi = LocalAppUi.current

    // Use NotificationViewModel (Hilt) to observe real notifications
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState(initial = 0)
    var showNotifications by remember { mutableStateOf(false) }

    // When opening the notifications popup, mark all as read
    LaunchedEffect(showNotifications) {
        if (showNotifications) {
            notificationViewModel.markAllRead()
        }
    }

    // Resolve topBar colors: prefer values from LocalAppUi but fall back to the requested hexes
    // Use the requested color B6BBC4 as the fallback top bar color
    val fallbackLight = listOf(Color(0xFFB6BBC4), Color(0xFFB6BBC4))
    val topBarGradientColors = appUi.topBarGradientColors.takeIf { list ->
        list.isNotEmpty() && list.none { it == Color.Unspecified }
    } ?: fallbackLight

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, list, card, topBar, add) = createRefs()
            // Topbar drawn as a gradient Box so it can switch per-theme
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .background(brush = Brush.linearGradient(colors = topBarGradientColors)))

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    ExpenseTextView(
                        text = "EXPENSE TRACKER",
                        style = Typography.titleLarge,
                        color = Color.White
                    )
                }
                // top-right menu (Chỉ còn: Đăng xuất)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Use fixed-size containers so clicks / ripples don't change layout
                    var expandedMenu by remember { mutableStateOf(false) }

                    // Bell with badge overlay: fixed 48.dp touch target
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = { showNotifications = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Thông báo",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (unreadCount > 0) {
                            // Badge: fixed size, positioned relative to the 48.dp box so it won't cause layout shifts.
                            // Use align + padding instead of Modifier.offset to avoid needing the offset import.
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 2.dp, end = 2.dp)
                                    .background(color = Color.Red, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Overflow menu icon: fixed 48.dp touch target to match bell
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { expandedMenu = true }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Thêm tùy chọn",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                            DropdownMenuItem(text = { ExpenseTextView(text = "Đăng xuất") }, onClick = {
                                expandedMenu = false
                                onLogoutClicked()
                            })
                        }
                    }
                }
            }

            val expenseTotal = expenses
            val expense = "" + Utils.formatCurrency(expenseTotal.filter { it.type != "Income" }.sumOf { it.amount })
            val income = "" + Utils.formatCurrency(expenseTotal.filter { it.type == "Income" }.sumOf { it.amount })
            val balance = "" + Utils.formatCurrency(expenseTotal.fold(0.0) { acc, e -> if (e.type == "Income") acc + e.amount else acc - e.amount })
            CardItem(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                balance = balance, income = income, expense = expense,
                // Force the card background to black so the balance card is always black
                cardBg = Color.Black
            )
            TransactionList(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(list) {
                        top.linkTo(card.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }, list = expenses, onSeeAllClicked = {
                    onSeeAllClicked()
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }, contentAlignment = Alignment.BottomEnd
            ) {
                MultiFloatingActionButton(modifier = Modifier, {
                    onAddExpenseClicked()
                }, {
                    onAddIncomeClicked()
                }, fabTint = appUi.fabIconTint)
            }
        }
    }

    // Notification popup dialog (Dialog ensures it won't exceed screen height and allows custom scrollable content)
    if (showNotifications) {
        // Large dialog (near full width, 65% screen height) with X close icon
        val config = LocalConfiguration.current
        val screenHeightDp = config.screenHeightDp
        val dialogHeight = (screenHeightDp * 0.65).dp

        Dialog(onDismissRequest = { showNotifications = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dialogHeight, max = dialogHeight)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ExpenseTextView(text = "Thông báo", style = Typography.titleLarge, color = Color.Black)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showNotifications = false }) {
                            MIcon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                    Spacer(modifier = Modifier.size(8.dp))

                    if (notifications.isEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            ExpenseTextView(text = "Không có thông báo")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(items = notifications, key = { it.id ?: 0 }) { n: NotificationEntity ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    ExpenseTextView(text = n.title, style = Typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.size(4.dp))
                                    ExpenseTextView(text = n.message, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.size(4.dp))
                                    ExpenseTextView(text = Utils.formatDateToHumanReadableForm(n.timestamp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiFloatingActionButton(
    modifier: Modifier,
    onAddExpenseClicked: () -> Unit,
    onAddIncomeClicked: () -> Unit,
    fabTint: Color = Color.Unspecified
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Secondary FABs
            AnimatedVisibility(visible = expanded) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                onAddIncomeClicked.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_income),
                            contentDescription = "Thêm thu nhập",
                            tint = fabTint
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                onAddExpenseClicked.invoke()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expense),
                            contentDescription = "Thêm chi tiêu",
                            tint = fabTint
                        )
                    }
                }
            }
            // Main FAB
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.primary)
                    .clickable {
                        expanded = !expanded
                    },
                contentAlignment = Alignment.Center
            ) {
                // Use Icon so we can tint it to onPrimary to match theme
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "nút thêm",
                    tint = fabTint,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun CardItem(
    modifier: Modifier,
    balance: String, income: String, expense: String,
    // Force the card background for the balance card to black as requested
    cardBg: Color = Color.Black
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column {
                ExpenseTextView(
                    text = "Tổng số dư",
                    style = Typography.titleMedium,
                    // Use white text over the black card
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                ExpenseTextView(
                    text = balance, style = Typography.headlineLarge, color = Color.White,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CardRowItem(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                title = "Thu nhập",
                amount = income,
                imaget = R.drawable.ic_income
            )
            Spacer(modifier = Modifier.size(8.dp))
            CardRowItem(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                title = "Chi tiêu",
                amount = expense,
                imaget = R.drawable.ic_expense
            )
        }

    }
}

@Composable
fun TransactionList(
    modifier: Modifier,
    list: List<ExpenseEntity>,
    title: String = "Giao dịch gần đây",
    onSeeAllClicked: () -> Unit
) {
    // Always display newest transactions at top. Dates are stored as dd/MM/yyyy strings; convert to millis to sort.
    val sorted = list.sortedByDescending { Utils.getMillisFromDate(it.date) }

    // Use fixed light-mode colors (no dark/light switching)
    val sectionTitleColor = Color.Black

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        item {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExpenseTextView(
                        text = title,
                        style = Typography.titleLarge,
                        color = sectionTitleColor,
                    )
                    if (title == "Giao dịch gần đây") {
                        ExpenseTextView(
                            text = "Xem tất cả",
                            color = MaterialTheme.colorScheme.primary,
                            style = Typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    onSeeAllClicked.invoke()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
        items(items = sorted,
            key = { item -> item.id ?: 0 }) { item ->
             val amount = if (item.type == "Income") item.amount else item.amount * -1

             TransactionItem(
                 title = item.title,
                 amount = Utils.formatCurrency(amount),
                 date = Utils.formatStringDateToMonthDayYear(item.date),
                 color = if (item.type == "Income") MaterialTheme.colorScheme.secondary else Red,
                 Modifier
             )
         }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun TransactionItem(
    title: String,
    amount: String,
    date: String,
    color: Color,
    modifier: Modifier
) {

    // Fixed light-mode title color
    val transactionTitleColor = Color.Black

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                ExpenseTextView(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = transactionTitleColor)
                Spacer(modifier = Modifier.size(6.dp))
                ExpenseTextView(text = date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        ExpenseTextView(
            text = amount,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterEnd),
            color = color
        )
    }
}

@Composable
fun CardRowItem(modifier: Modifier, title: String, amount: String, imaget: Int) {
    Column(modifier = modifier) {
        Row {

            Icon(
                painter = painterResource(id = imaget),
                contentDescription = null,
                // Ensure icons are visible on the black card
                tint = Color.White
            )
            Spacer(modifier = Modifier.size(8.dp))
            ExpenseTextView(text = title, style = Typography.bodyLarge, color = Color.White)
        }
        Spacer(modifier = Modifier.size(4.dp))
        ExpenseTextView(text = amount, style = Typography.titleLarge, color = Color.White)
    }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel(), authViewModel: com.codewithfk.expensetracker.android.feature.auth.AuthViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack()
                HomeNavigationEvent.NavigateToSeeAll -> {
                    navController.navigate("/all_transactions")
                }

                HomeNavigationEvent.NavigateToAddIncome -> {
                    navController.navigate("/add_income")
                }

                HomeNavigationEvent.NavigateToAddExpense -> {
                    navController.navigate("/add_exp")
                }

                else -> {}
            }
        }
    }

    val state = viewModel.expenses.collectAsState(initial = emptyList())

    HomeContent(
        expenses = state.value,
        onSeeAllClicked = { viewModel.onEvent(HomeUiEvent.OnSeeAllClicked) },
        onAddExpenseClicked = { viewModel.onEvent(HomeUiEvent.OnAddExpenseClicked) },
        onAddIncomeClicked = { viewModel.onEvent(HomeUiEvent.OnAddIncomeClicked) },
        onLogoutClicked = {
            authViewModel.clearRemember()
            navController.navigate("/login") {
                popUpTo("/home") { inclusive = true }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeContent() {
    val sample = listOf(
        ExpenseEntity(id = 1, title = "Netflix", amount = 120000.0, date = "01/12/2025", type = "Expense"),
        ExpenseEntity(id = 2, title = "Lương", amount = 5000000.0, date = "01/12/2025", type = "Income")
    )
    // Light preview (fixed light mode)
    ExpenseTrackerAndroidTheme(darkTheme = false) {
        HomeContent(
            expenses = sample,
            onSeeAllClicked = {},
            onAddExpenseClicked = {},
            onAddIncomeClicked = {},
            onLogoutClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeContentDark() {
    val sample = listOf(
        ExpenseEntity(id = 1, title = "Netflix", amount = 120000.0, date = "01/12/2025", type = "Expense"),
        ExpenseEntity(id = 2, title = "Lương", amount = 5000000.0, date = "01/12/2025", type = "Income")
    )
    // Also preview in the same fixed light theme for consistency
    ExpenseTrackerAndroidTheme(darkTheme = false) {
        HomeContent(
            expenses = sample,
            onSeeAllClicked = {},
            onAddExpenseClicked = {},
            onAddIncomeClicked = {},
            onLogoutClicked = {}
        )
    }
}
