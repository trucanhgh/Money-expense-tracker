package com.codewithfk.expensetracker.android.feature.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codewithfk.expensetracker.android.data.model.CategorySummary
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import java.util.Locale

/**
 * Simple row composable for displaying a category summary with an overflow menu
 * that exposes Edit and Delete actions.
 */
@Composable
fun CategoryRow(
    summary: CategorySummary,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // menu state
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // left: name
        ExpenseTextView(text = summary.name, style = MaterialTheme.typography.bodyLarge)

        // right: total + overflow menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            val totalText = String.format(Locale.getDefault(), "%,.0f", summary.total)
            // show total with color depending on sign
            val color = if (summary.total < 0) {
                // negative: use app Red
                com.codewithfk.expensetracker.android.ui.theme.Red
            } else {
                // positive: use theme secondary (mapped to the user's muted palette)
                MaterialTheme.colorScheme.secondary
            }
            Text(text = totalText, color = color, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))

            IconButton(onClick = { menuExpanded = true }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
            }

            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Sửa") }, onClick = {
                    menuExpanded = false
                    onEdit()
                })
                DropdownMenuItem(text = { Text("Xóa") }, onClick = {
                    menuExpanded = false
                    onDelete()
                })
            }
        }
    }
}
