package com.example.hastakala.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.data.Inventory
import com.example.hastakala.viewmodel.SaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(viewModel: SaleViewModel) {

    val inventory by viewModel.inventory.collectAsState()

    var showRestockDialog by remember {
        mutableStateOf(false)
    }

    var selectedItemForRestock by remember {
        mutableStateOf<Inventory?>(null)
    }

    var restockAmount by remember {
        mutableIntStateOf(5)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    val purple = Color(0xFF5D3587)
    val gold = Color(0xFFFFD700)

    // Alphabetical Order - Only items currently in stock
    val filteredInventory = inventory
        .filter {
            it.item.contains(searchQuery, ignoreCase = true) && it.quantity > 0
        }
        .sortedBy {
            it.item.lowercase()
        }

    val totalStock = inventory.sumOf {
        it.quantity
    }

    // Critical Stock Items - Only items currently in stock but low
    val criticalStockItems = inventory
        .filter {
            it.quantity in 1..3
        }
        .sortedBy {
            it.item.lowercase()
        }

    Scaffold(
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F5FF),
                            Color.White
                        )
                    )
                )
        ) {

            // ===================================================
            // SUMMARY CARD
            // ===================================================

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = purple
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            "Total Products",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )

                        Text(
                            "${inventory.size}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            "Total Stock: $totalStock",
                            color = gold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                gold.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // ===================================================
            // LOW STOCK PRODUCTS
            // ===================================================

            if (criticalStockItems.isNotEmpty()) {

                Text(
                    text = "Low Stock Products",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        bottom = 8.dp
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(criticalStockItems) { item ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            Color.Red.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {

                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        item.item,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        "Only ${item.quantity} left",
                                        color = Color.Red,
                                        fontSize = 13.sp
                                    )
                                }

                                Button(
                                    onClick = {

                                        selectedItemForRestock = item
                                        restockAmount = 5
                                        showRestockDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = purple
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {

                                    Text("Restock")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===================================================
            // SEARCH BAR
            // ===================================================

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = {
                    Text("Search Product")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===================================================
            // PRODUCT LIST
            // ===================================================

            if (filteredInventory.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp),
                            tint = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "No Products Found",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    items(filteredInventory) { item ->

                        val lowStock = item.quantity <= 3

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(
                                                purple.copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Icon(
                                            Icons.Default.List,
                                            contentDescription = null,
                                            tint = purple
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        Text(
                                            item.item,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            "Color: ${item.color}",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (lowStock)
                                            Color.Red.copy(alpha = 0.12f)
                                        else
                                            Color(0xFF4CAF50).copy(alpha = 0.12f)
                                    ) {

                                        Text(
                                            "${item.quantity} pcs",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            color = if (lowStock)
                                                Color.Red
                                            else
                                                Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = {
                                        (item.quantity / 20f).coerceAtMost(1f)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = if (lowStock)
                                        Color.Red
                                    else
                                        Color(0xFF4CAF50),
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {

                                        selectedItemForRestock = item
                                        restockAmount = 5
                                        showRestockDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = purple
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {

                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text("Restock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ===================================================
    // RESTOCK DIALOG
    // ===================================================

    if (showRestockDialog && selectedItemForRestock != null) {

        AlertDialog(

            onDismissRequest = {
                showRestockDialog = false
            },

            title = {
                Text(
                    "Restock Product",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    Text(
                        "Product: ${selectedItemForRestock!!.item}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Color: ${selectedItemForRestock!!.color}"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Current Stock: ${selectedItemForRestock!!.quantity}"
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        // DECREASE
                        FilledTonalIconButton(
                            onClick = {
                                restockAmount--
                            }
                        ) {

                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease"
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            if (restockAmount > 0)
                                "$restockAmount"
                            else
                                "$restockAmount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (restockAmount >= 0)
                                Color(0xFF2E7D32)
                            else
                                Color.Red
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        // INCREASE
                        FilledTonalIconButton(
                            onClick = {
                                restockAmount++
                            }
                        ) {

                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase"
                            )
                        }
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val newQty =
                            selectedItemForRestock!!.quantity + restockAmount

                        if (newQty >= 0) {

                            viewModel.restock(
                                selectedItemForRestock!!.item,
                                selectedItemForRestock!!.color,
                                restockAmount
                            )
                        }

                        // Go back to normal product list
                        showRestockDialog = false
                        selectedItemForRestock = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple
                    )
                ) {

                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showRestockDialog = false
                        selectedItemForRestock = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}