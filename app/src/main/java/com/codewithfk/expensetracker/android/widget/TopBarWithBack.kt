package com.codewithfk.expensetracker.android.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.codewithfk.expensetracker.android.R

/**
 * Small reusable top bar with a back icon on the start, a centered title and optional trailing icon.
 * Use this to keep a consistent back button across screens.
 */
@Composable
fun TopBarWithBack(
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Image(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Quay lại",
            modifier = Modifier.align(Alignment.CenterStart).clickable { onBack() },
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )

        Box(modifier = Modifier.align(Alignment.Center)) {
            title()
        }

        trailingIcon?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) { it() }
        }
    }
}
