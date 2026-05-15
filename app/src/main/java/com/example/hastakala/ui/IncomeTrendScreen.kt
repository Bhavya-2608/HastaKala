package com.example.hastakala.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.hastakala.viewmodel.SaleViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeTrendScreen(
    viewModel: SaleViewModel,
    onBack: () -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Days") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val selectedDateStr = sdf.format(Date(selectedMillis))

    // Filter sales based on selected date and tab
    val filteredSales = remember(sales, selectedTab, selectedMillis) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedMillis
        
        when (selectedTab) {
            "Days" -> {
                val day = calendar.get(Calendar.DAY_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                sales.filter { 
                    val sCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    sCal.get(Calendar.DAY_OF_YEAR) == day && sCal.get(Calendar.YEAR) == year
                }
            }
            "Week" -> {
                val week = calendar.get(Calendar.WEEK_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                sales.filter { 
                    val sCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    sCal.get(Calendar.WEEK_OF_YEAR) == week && sCal.get(Calendar.YEAR) == year
                }
            }
            "Month" -> {
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)
                sales.filter { 
                    val sCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    sCal.get(Calendar.MONTH) == month && sCal.get(Calendar.YEAR) == year
                }
            }
            else -> sales
        }
    }

    val displayTotal = filteredSales.sumOf { it.price * it.quantity }
    
    val titlePrefix = when(selectedTab) {
        "Month" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedMillis))
        "Week" -> "Week ${Calendar.getInstance().apply { timeInMillis = selectedMillis }.get(Calendar.WEEK_OF_YEAR)}, ${Calendar.getInstance().apply { timeInMillis = selectedMillis }.get(Calendar.YEAR)}"
        else -> selectedDateStr
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Income Trend", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF5D3587)
                )
            )
        }
    ) { innerPadding ->
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(horizontal = 20.dp)
        ) {
            // Tab Selector
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        listOf("Days", "Week", "Month").forEach { tab ->
                            val isSelected = selectedTab == tab
                            Surface(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF5D3587) else Color.Transparent,
                                onClick = { selectedTab = tab }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        tab,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Total Income Summary for Selected Period
            item {
                Text(titlePrefix, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    "₹ ${String.format(Locale.getDefault(), "%,.0f", displayTotal)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Chart Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    val chartMap = when(selectedTab) {
                        "Days" -> filteredSales.groupBy { 
                            val c = Calendar.getInstance().apply { timeInMillis = it.date }
                            "${c.get(Calendar.HOUR_OF_DAY)}h"
                        }.mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                        "Week" -> filteredSales.groupBy { 
                            val c = Calendar.getInstance().apply { timeInMillis = it.date }
                            SimpleDateFormat("EEE", Locale.getDefault()).format(c.time)
                        }.mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                        "Month" -> filteredSales.groupBy { 
                            val c = Calendar.getInstance().apply { timeInMillis = it.date }
                            "Day ${c.get(Calendar.DAY_OF_MONTH)}"
                        }.mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                        else -> emptyMap()
                    }
                    IncomeTrendBarChart(chartMap)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Breakdown List of Transactions for the period
            item {
                Text("Transactions Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(filteredSales.sortedByDescending { it.date }) { sale ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${sale.item} (${sale.color})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sale.date)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("₹ ${String.format(Locale.getDefault(), "%,.0f", sale.price * sale.quantity)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Divider(color = Color(0xFFF0F0F0))
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun IncomeTrendBarChart(data: Map<String, Double>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = android.graphics.Color.parseColor("#F0F0F0")
                axisLeft.granularity = 100f
                axisLeft.isGranularityEnabled = true
                axisLeft.axisMinimum = 0f
                
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = data.values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value.toFloat())
            }
            val dataSet = BarDataSet(entries, "Income").apply {
                color = android.graphics.Color.parseColor("#A294F9") // Light purple
                valueTextSize = 10f
                setDrawValues(false)
            }
            chart.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.keys.toList())
            chart.xAxis.labelCount = data.size
            chart.invalidate()
        }
    )
}
