package com.example.hastakala.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAnalyticsScreen(
    viewModel: SaleViewModel,
    onBack: () -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Month") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

    // Filter sales based on selected date and tab
    val filteredSales = remember(sales, selectedTab, selectedMillis) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedMillis
        
        when (selectedTab) {
            "Day" -> {
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
    
    val productRevenueMap = filteredSales.groupBy { it.item }
        .mapValues { it.value.sumOf { s -> s.price * s.quantity } }
        .toList()
        .sortedByDescending { it.second }

    val titlePrefix = when(selectedTab) {
        "Month" -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(selectedMillis))
        "Week" -> "Week ${Calendar.getInstance().apply { timeInMillis = selectedMillis }.get(Calendar.WEEK_OF_YEAR)}, ${Calendar.getInstance().apply { timeInMillis = selectedMillis }.get(Calendar.YEAR)}"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedMillis))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sales Analytics", color = Color.White) },
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
                        listOf("Day", "Week", "Month").forEach { tab ->
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
                                        style = MaterialTheme.typography.labelMedium,
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

            // Pie Chart Section
            item {
                Text("Sales by Product: $titlePrefix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (productRevenueMap.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No data for this period", color = Color.Gray)
                            }
                        } else {
                            SalesPieChart(productRevenueMap)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Sales breakdown list
            item {
                Text("Period Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (productRevenueMap.isEmpty()) {
                item {
                    Text("No transactions found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }
            } else {
                items(productRevenueMap) { (product, revenue) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("₹ ${String.format(Locale.getDefault(), "%,.0f", revenue)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color(0xFFF0F0F0))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SalesPieChart(data: List<Pair<String, Double>>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                holeRadius = 40f
                transparentCircleRadius = 45f
                setEntryLabelColor(android.graphics.Color.BLACK)
                setEntryLabelTextSize(10f)
                legend.isEnabled = true
                legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.CENTER
                legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.VERTICAL
                legend.setDrawInside(false)
            }
        },
        update = { chart ->
            val entries = data.map { (label, value) ->
                PieEntry(value.toFloat(), label)
            }
            val dataSet = PieDataSet(entries, "").apply {
                colors = ColorTemplate.COLORFUL_COLORS.toList() + ColorTemplate.JOYFUL_COLORS.toList()
                sliceSpace = 2f
                valueTextSize = 12f
                valueTextColor = android.graphics.Color.BLACK
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}
