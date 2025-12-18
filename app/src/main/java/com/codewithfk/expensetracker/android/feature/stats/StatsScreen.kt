package com.codewithfk.expensetracker.android.feature.stats

import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.codewithfk.expensetracker.android.R
import com.codewithfk.expensetracker.android.feature.home.TransactionList
import com.codewithfk.expensetracker.android.utils.Utils
import com.codewithfk.expensetracker.android.widget.ExpenseTextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Stateless content for Stats screen. Accepts providers for flows and a mapper to convert summaries to Entries.
 */
@Composable
fun StatsContent(
    entriesSummaryFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseSummary>>,
    topEntriesFlowProvider: () -> Flow<List<com.codewithfk.expensetracker.android.data.model.ExpenseEntity>>,
    entriesMapper: (List<com.codewithfk.expensetracker.android.data.model.ExpenseSummary>) -> List<Entry>
) {
    val isPreview = LocalInspectionMode.current
    val dataState by entriesSummaryFlowProvider().collectAsState(initial = emptyList())
    val topExpense by topEntriesFlowProvider().collectAsState(initial = emptyList())
    Column(modifier = Modifier.padding(16.dp)) {
        // header area
        // Use a simple scaffold-like header
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart).clickable { /* no-op in content */ },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
            )
            ExpenseTextView(
                text = "Thống kê",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Image(
                painter = painterResource(id = R.drawable.dots_menu),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
            )
        }

        val entries = entriesMapper(dataState)

        if (isPreview) {
            // In preview mode show a simple placeholder box instead of AndroidView
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)) {
                ExpenseTextView(text = "[Chart preview]")
            }
        } else {
            LineChart(entries = entries)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TransactionList(Modifier, list = topExpense, "Chi tiêu hàng đầu", onSeeAllClicked = {})
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun StatsScreen(navController: NavController, viewModel: StatsViewModel = hiltViewModel()) {
    StatsContent(
        entriesSummaryFlowProvider = { viewModel.entries },
        topEntriesFlowProvider = { viewModel.topEntries },
        entriesMapper = { summaries -> viewModel.getEntriesForChart(summaries) }
    )
}

@Suppress("InflateParams")
@Composable
fun LineChart(entries: List<Entry>) {
    val context = LocalContext.current
    // Capture theme colors (toArgb) here in the @Composable scope so we don't call MaterialTheme inside the AndroidView update lambda
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val onBackgroundColorArgb = MaterialTheme.colorScheme.onBackground.toArgb()
    AndroidView(
        factory = {
            // Inflate into a temporary FrameLayout parent to avoid passing null
            val parent = android.widget.FrameLayout(context)
            val view = LayoutInflater.from(context).inflate(R.layout.stats_line_chart, parent, false)
            view
        }, modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) { view ->
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        val dataSet = LineDataSet(entries, "Chi tiêu").apply {
            color = primaryColorArgb
            valueTextColor = onBackgroundColorArgb
            lineWidth = 3f
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 12f
            valueTextColor = onBackgroundColorArgb
            val drawable = ContextCompat.getDrawable(context, R.drawable.char_gradient)
            drawable?.let {
                fillDrawable = it
            }

        }

        lineChart.xAxis.valueFormatter =
            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return Utils.formatDateForChart(value.toLong())
                }
            }
        lineChart.data = com.github.mikephil.charting.data.LineData(dataSet)
        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false
        lineChart.axisRight.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.invalidate()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStatsContent() {
    val sampleSummaries = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseSummary(type = "Expense", date = "01/12/2025", total_amount = 100000.0),
        com.codewithfk.expensetracker.android.data.model.ExpenseSummary(type = "Expense", date = "02/12/2025", total_amount = 150000.0)
    )
    val sampleExpenses = listOf(
        com.codewithfk.expensetracker.android.data.model.ExpenseEntity(id = 1, title = "Ăn uống", amount = 120000.0, date = "01/12/2025", type = "Expense")
    )
    StatsContent(
        entriesSummaryFlowProvider = { flowOf(sampleSummaries) },
        topEntriesFlowProvider = { flowOf(sampleExpenses) },
        entriesMapper = { summaries ->
            // simple mapper for preview
            summaries.mapIndexed { idx, s -> Entry(idx.toFloat(), s.total_amount.toFloat()) }
        }
    )
}
