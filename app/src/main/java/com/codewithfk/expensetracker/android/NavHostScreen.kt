package com.codewithfk.expensetracker.android

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codewithfk.expensetracker.android.feature.add_expense.AddExpense
import com.codewithfk.expensetracker.android.feature.home.HomeScreen
import com.codewithfk.expensetracker.android.feature.stats.StatsScreen
import com.codewithfk.expensetracker.android.feature.transactionlist.TransactionListScreen
import com.codewithfk.expensetracker.android.feature.category.CategoryListScreen
import com.codewithfk.expensetracker.android.feature.category.CategoryDetailScreen
import com.codewithfk.expensetracker.android.feature.auth.LoginScreen
import com.codewithfk.expensetracker.android.feature.auth.RegisterScreen
import com.codewithfk.expensetracker.android.feature.auth.SettingsScreen
import com.codewithfk.expensetracker.android.feature.goal.GoalListScreen
import com.codewithfk.expensetracker.android.ui.theme.Zinc

@Composable
fun NavHostScreen() {
    val navController = rememberNavController()
    var bottomBarVisibility by remember {
        mutableStateOf(true)

    }
    Scaffold(bottomBar = {
        AnimatedVisibility(visible = bottomBarVisibility) {
            NavigationBottomBar(
                navController = navController,
                items = listOf(
                    NavItem(route = "/home", icon = R.drawable.ic_home),
                    // Use a proper vector drawable for the categories icon to avoid painterResource crashes
                    NavItem(route = "/categories", icon = R.drawable.ic_dashboard),
                    NavItem(route = "/goals", icon = R.drawable.ic_goal),
                    NavItem(route = "/stats", icon = R.drawable.ic_stats)
                )
            )
        }
    }) {
        NavHost(
            navController = navController,
            startDestination = "/login",
            modifier = Modifier.padding(it)
        ) {
            composable(route = "/login") {
                bottomBarVisibility = false
                LoginScreen(navController)
            }

            composable(route = "/register") {
                bottomBarVisibility = false
                RegisterScreen(navController)
            }

            composable(route = "/home") {
                bottomBarVisibility = true
                HomeScreen(navController)
            }

            composable(route = "/add_income") {
                bottomBarVisibility = false
                AddExpense(navController, isIncome = true)
            }
            composable(route = "/add_exp") {
                bottomBarVisibility = false
                AddExpense(navController, isIncome = false)
            }
            // Add route that accepts a prefill string for the title/category
            composable(route = "/add_exp/{prefill}") { backStackEntry ->
                bottomBarVisibility = false
                val prefill = backStackEntry.arguments?.getString("prefill")?.let { java.net.URLDecoder.decode(it, "UTF-8") } ?: ""
                AddExpense(navController, isIncome = false, initialPrefill = prefill)
            }

            composable(route = "/stats") {
                bottomBarVisibility = true
                StatsScreen(navController)
            }
            composable(route = "/all_transactions") {
                bottomBarVisibility = true // Show the bottom bar if you want it visible
                TransactionListScreen(navController)
            }

            composable(route = "/categories") {
                bottomBarVisibility = true
                CategoryListScreen(navController)
            }

            composable(route = "/category_detail/{name}") { backStackEntry ->
                bottomBarVisibility = true
                val name = backStackEntry.arguments?.getString("name")
                CategoryDetailScreen(navController, name)
            }

            // Goals
            composable(route = "/goals") {
                bottomBarVisibility = true
                GoalListScreen(navController)
            }

            composable(route = "/goal_detail/{name}") { backStackEntry ->
                bottomBarVisibility = true
                val name = backStackEntry.arguments?.getString("name")
                // GoalDetailScreen handles its own add actions; call directly
                com.codewithfk.expensetracker.android.feature.goal.GoalDetailScreen(navController, name)
            }

            composable(route = "/settings") {
                bottomBarVisibility = false
                SettingsScreen(navController)
            }
        }
    }
}


data class NavItem(
    val route: String,
    val icon: Int
)

@Composable
fun NavigationBottomBar(
    navController: NavController,
    items: List<NavItem>
) {
    // Bottom Navigation Bar
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    BottomAppBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // Force icon size to 36.dp to match `ic_home` vector dimensions so all nav icons appear uniform
                    Icon(painter = painterResource(id = item.icon), contentDescription = null, modifier = Modifier.size(36.dp))
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Zinc,
                    selectedIconColor = Zinc,
                    unselectedTextColor = Color.Gray,
                    unselectedIconColor = Color.Gray
                )
            )
        }
    }
}
