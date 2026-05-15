package com.example.hastakala.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.ui.components.AddInventoryDialog
import com.example.hastakala.ui.utils.ArtisanData
import com.example.hastakala.viewmodel.SaleViewModel

@Composable
fun InventoryScreen(viewModel: SaleViewModel) {

    val inventoryList by viewModel.inventory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Alphabetical Sorting
    val groupedInventory = inventoryList
        .filter {
            it.item.contains(searchQuery, ignoreCase = true)
        }
        .groupBy { it.item }
        .toSortedMap()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<String?>(null) }
    var productToEdit by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    "Artisan Inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFBC4A3C),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${inventoryList.size} Products Available",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            placeholder = {
                Text("Search products...")
            },
            shape = RoundedCornerShape(18.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            items(groupedInventory.keys.toList()) { itemName ->

                val items = groupedInventory[itemName] ?: emptyList()

                InventoryCard(
                    itemName = itemName,
                    items = items,
                    onDelete = {
                        productToDelete = itemName
                    },
                    onEdit = {
                        productToEdit = itemName
                    }
                )
            }
        }
    }

    // ADD PRODUCT DIALOG
    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { item, color, qty, price ->
                viewModel.restock(item, color, qty, price)
                showAddDialog = false
            },
            brandDark = MaterialTheme.colorScheme.primary,
            brandPrimary = MaterialTheme.colorScheme.tertiary
        )
    }

    // DELETE DIALOG
    if (productToDelete != null) {

        AlertDialog(
            onDismissRequest = {
                productToDelete = null
            },
            title = {
                Text("Delete Product")
            },
            text = {
                Text("Remove $productToDelete from inventory?")
            },
            confirmButton = {

                Button(
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        productToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // EDIT DIALOG
    if (productToEdit != null) {

        val productItems = inventoryList.filter { it.item == productToEdit }

        var newName by remember { mutableStateOf(productToEdit!!) }
        var newPrice by remember {
            mutableStateOf(productItems.firstOrNull()?.price?.toString() ?: "0.0")
        }

        var showAddColorSection by remember { mutableStateOf(false) }
        var selectedNewColors by remember { mutableStateOf(setOf<com.example.hastakala.ui.utils.ColorItem>()) }
        var addColorQty by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = {
                productToEdit = null
            },
            title = {
                Text("Edit Product")
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Product Name")
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = {
                            newPrice = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Price (₹)")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Current Colors", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    productItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.color} (${item.quantity} pcs)")
                            IconButton(onClick = {
                                viewModel.deleteColor(productToEdit!!, item.color)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!showAddColorSection) {
                        TextButton(onClick = { showAddColorSection = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add More Colors")
                        }
                    } else {
                        Text("Select New Colors", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

                        ArtisanData.colors.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { colorItem ->
                                    val isSelected = selectedNewColors.contains(colorItem)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedNewColors = if (isSelected) {
                                                selectedNewColors - colorItem
                                            } else {
                                                selectedNewColors + colorItem
                                            }
                                        },
                                        label = {
                                            Text(colorItem.name, fontSize = 10.sp)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addColorQty,
                            onValueChange = {
                                addColorQty = it
                            },
                            label = {
                                Text("Quantity for new colors")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {

                Button(
                    onClick = {

                        if (newName.isNotBlank()) {
                            // 1. Rename if changed
                            if (newName != productToEdit) {
                                viewModel.renameProduct(productToEdit!!, newName)
                            }

                            // 2. Update Price
                            val priceVal = newPrice.toDoubleOrNull() ?: 0.0
                            viewModel.updatePrice(newName, priceVal)

                            // 3. Add new colors if any
                            if (selectedNewColors.isNotEmpty()) {
                                val qty = addColorQty.toIntOrNull() ?: 0
                                selectedNewColors.forEach { colorItem ->
                                    viewModel.restock(newName, colorItem.name, qty, priceVal)
                                }
                            }
                        }

                        productToEdit = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        productToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InventoryCard(
    itemName: String,
    items: List<com.example.hastakala.data.Inventory>,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    val totalStock = items.sumOf { it.quantity }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            // TOP ROW
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        itemName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Stock: $totalStock",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        val price = items.firstOrNull()?.price ?: 0.0
                        Text(
                            "₹${price.toInt()}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null)
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STOCK BAR
            LinearProgressIndicator(
                progress = {
                    (totalStock / 100f).coerceAtMost(1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
            )

            Spacer(modifier = Modifier.height(18.dp))

            // COLOR VARIATIONS
            Text(
                "Available Colors",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items.forEach {

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {

                        Text(
                            text = "${it.color} (${it.quantity})",
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}