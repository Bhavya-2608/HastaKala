package com.example.hastakala.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.viewmodel.SaleViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SaleViewModel,
    username: String,
    onNavigateToIncomeTrend: () -> Unit,
    onNavigateToSalesAnalytics: () -> Unit,
    onNavigateToQuickBill: () -> Unit,
    onNavigateToRestock: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val lowStockCount = inventory.count { it.quantity <= 3 }
    var selectedBestSellerPeriod by remember { mutableStateOf("Week") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 20.dp)
    ) {
        // Welcome Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Welcome to Hasta-Kala Shop,", style = MaterialTheme.typography.titleMedium, color = Color.Gray, fontSize = 18.sp)
                Text("${username.ifEmpty { "Artisan" }} 👋", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Low Stock Alert Card
        if (lowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRestock() }
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Low Stock Alert",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$lowStockCount items are running low. Tap to restock.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Today's Sales Card
        item {
            TodaySalesCard(amount = stats.todaySales, percentage = stats.percentageChange)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryStatCard(
                    title = "Total Sales",
                    value = "₹ ${String.format(Locale.getDefault(), "%,.0f", stats.monthTotalIncome)}",
                    subtitle = "This Month",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = "Total Orders",
                    value = "${stats.monthTotalOrders}",
                    subtitle = "This Month",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Quick Actions
        item {
            Text("Sales Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionItem(
                        title = "Income Trend", 
                        icon = Icons.Default.ShoppingCart, 
                        color = Color(0xFF5D3587),
                        modifier = Modifier.clickable { onNavigateToIncomeTrend() }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    QuickActionItem(
                        title = "Top Selling Product", 
                        icon = Icons.Default.Star, 
                        color = Color(0xFFE67E22),
                        modifier = Modifier.clickable { onNavigateToSalesAnalytics() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Best Selling Section with Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Best Selling", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        listOf("Week", "Month").forEach { period ->
                            val isSelected = selectedBestSellerPeriod == period
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF5D3587) else Color.Transparent,
                                modifier = Modifier.clickable { selectedBestSellerPeriod = period }
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        period,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val currentBestSellers = if (selectedBestSellerPeriod == "Week") stats.weekBestSellers else stats.monthBestSellers
            
            if (currentBestSellers.isEmpty()) {
                Text("No data available for this ${selectedBestSellerPeriod.lowercase()}.", color = Color.Gray)
            } else {
                currentBestSellers.forEach { (name, count, price) ->
                    BestSellingProductCard(name = name, soldCount = count, price = price)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TodaySalesCard(amount: Double, percentage: Int) {
    val isIncrease = percentage >= 0
    val cardBackground = if (isIncrease) {
        Brush.linearGradient(colors = listOf(Color(0xFF27AE60), Color(0xFF2ECC71)))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFFE74C3C), Color(0xFFC0392B)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackground)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text("Today's Sales", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("₹ ${String.format(Locale.getDefault(), "%,.0f", amount)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val prefix = if (isIncrease) "+" else ""
                Text("$prefix$percentage% vs yesterday", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
            
            val icon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown
            val lineTint = if (isIncrease) Color(0xFFB9F6CA) else Color(0xFFFFD7D7)
            
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .alpha(0.4f),
                tint = lineTint
            )
        }
    }
}

@Composable
fun SummaryStatCard(title: String, value: String, subtitle: String = "", modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(12.dp),
            color = color
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.padding(16.dp), tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BestSellingProductCard(name: String, soldCount: Int, price: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹ ${price.toInt()}", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sold: $soldCount", color = Color.Gray, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
