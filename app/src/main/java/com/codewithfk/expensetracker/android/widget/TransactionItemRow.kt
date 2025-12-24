package com.codewithfk.expensetracker.android.widget

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.codewithfk.expensetracker.android.ui.theme.Red

/**
 * Shared transaction row used across Home, Transaction list, Category and Goal screens.
 * Matches the typography, spacing and colors used in Home -> "Giao dịch gần đây".
 */
@Composable
fun TransactionItemRow(
    title: String,
    amount: String,
    date: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    // Title color is fixed to black in the app's light theme
    val transactionTitleColor = Color.Black
    val amountColor = if (isIncome) MaterialTheme.colorScheme.secondary else Red

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                ExpenseTextView(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = transactionTitleColor)
                Spacer(modifier = Modifier.height(6.dp))
                ExpenseTextView(text = date, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        ExpenseTextView(
            text = amount,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterEnd),
            color = amountColor
        )
    }
}

