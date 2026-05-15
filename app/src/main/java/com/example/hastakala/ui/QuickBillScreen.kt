package com.example.hastakala.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.ui.theme.*
import com.example.hastakala.ui.utils.ArtisanData
import com.example.hastakala.ui.utils.BillItem
import com.example.hastakala.ui.utils.ColorItem
import com.example.hastakala.ui.utils.ProductItem
import com.example.hastakala.viewmodel.SaleViewModel

val QuickBillPurple = Color(0xFF5D3587)

@Composable
fun QuickBillScreen(
    viewModel: SaleViewModel,
    onNavigateToInventory: () -> Unit,
    onBack: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()
    
    // Products from inventory
    val products = remember(inventory) {
        val inStockInventory = inventory.filter { it.quantity > 0 }
        val uniqueItems = inStockInventory.map { it.item }.distinct()
        
        uniqueItems.map { name ->
            val icon = when {
                name.contains("Bag", true) -> Icons.Default.ShoppingCart
                name.contains("Keychain", true) -> Icons.Default.Favorite
                name.contains("Pouch", true) -> Icons.Default.Star
                name.contains("Hanging", true) -> Icons.Default.Home
                else -> Icons.Default.List
            }
            val price = inStockInventory.find { it.item == name }?.price ?: 0.0
            ProductItem(name, icon, price)
        }
    }

    var selectedProduct by remember { mutableStateOf<ProductItem?>(null) }
    var selectedColor by remember { mutableStateOf<ColorItem?>(null) }
    var quantity by remember { mutableIntStateOf(1) }
    var editablePrice by remember { mutableStateOf("") }
    val billItems by viewModel.billItems.collectAsState()
    var showSuccess by remember { mutableStateOf(false) }

    val colors = ArtisanData.colors
    val availableColors = remember(selectedProduct, inventory) {
        if (selectedProduct == null) emptyList<ColorItem>()
        else {
            val stockForProduct = inventory.filter { it.item == selectedProduct!!.name && it.quantity > 0 }
            val stockColorNames = stockForProduct.map { it.color }
            colors.filter { it.name in stockColorNames }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Select Product
            Text("1. Select Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(products) { product ->
                    val isSelected = selectedProduct == product
                    ProductCardImage(
                        product = product,
                        isSelected = isSelected,
                        onClick = {
                            selectedProduct = product
                            selectedColor = null
                            quantity = 1
                            editablePrice = product.price.toInt().toString()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Select Color / Design
            if (availableColors.isNotEmpty()) {
                Text("2. Select Color / Design", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(availableColors) { colorItem ->
                        val isSelected = selectedColor == colorItem
                        ColorSwatchCircle(
                            colorItem = colorItem,
                            isSelected = isSelected,
                            onClick = { selectedColor = colorItem }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Quantity & Price
            Text("3. Quantity & Price", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Quantity
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quantity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Text("-", style = MaterialTheme.typography.headlineSmall, color = QuickBillPurple)
                            }
                            Text("$quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { quantity++ }) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = QuickBillPurple)
                            }
                        }
                    }
                }
                // Price
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price (₹)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    OutlinedTextField(
                        value = editablePrice,
                        onValueChange = { editablePrice = it },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuickBillPurple,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        textStyle = TextStyle(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Add to Bill Button
            Button(
                onClick = {
                    if (selectedProduct != null && selectedColor != null) {
                        val price = editablePrice.toDoubleOrNull() ?: 0.0
                        viewModel.addToBill(BillItem(selectedProduct!!, selectedColor!!, quantity, price))
                        // Reset selection for next item
                        selectedProduct = null
                        selectedColor = null
                        quantity = 1
                        editablePrice = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = QuickBillPurple),
                shape = RoundedCornerShape(8.dp),
                enabled = selectedProduct != null && selectedColor != null
            ) {
                Text("ADD TO BILL")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Add to Bill (List)
            Text("4. Add to Bill", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                billItems.forEach { item ->
                    BillItemCard(
                        item = item,
                        onRemove = { viewModel.removeFromBill(item) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Total & Save
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", style = MaterialTheme.typography.titleLarge)
                val total = billItems.sumOf { it.price * it.quantity }
                Text("₹${total.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (billItems.isNotEmpty()) {
                        billItems.forEach { item ->
                            viewModel.addSale(item.product.name, item.color.name, item.price, item.quantity)
                        }
                        showSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QuickBillPurple),
                shape = RoundedCornerShape(8.dp),
                enabled = billItems.isNotEmpty()
            ) {
                Text("SAVE SALE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { 
                    showSuccess = false
                    viewModel.clearBill()
                    onBack()
                },
                title = { Text("Success") },
                text = { Text("Sale saved successfully!") },
                confirmButton = {
                    Button(onClick = { 
                        showSuccess = false
                        viewModel.clearBill()
                        onBack()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCardImage(
    product: ProductItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp).clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier
                .size(90.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) QuickBillPurple else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF9F9F9)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Image placeholder
                Icon(
                    product.icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp),
                    tint = if (isSelected) QuickBillPurple else Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp,
            color = if (isSelected) QuickBillPurple else Color.Black
        )
    }
}

@Composable
fun ColorSwatchCircle(
    colorItem: ColorItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .then(
                    if (isSelected) Modifier.border(2.dp, QuickBillPurple, CircleShape).padding(4.dp)
                    else Modifier
                )
                .clip(CircleShape)
                .background(colorItem.color)
                .then(
                    if (colorItem.color == Color.White) Modifier.border(1.dp, Color.LightGray, CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = colorItem.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) QuickBillPurple else Color.Gray
        )
    }
}

@Composable
fun BillItemCard(
    item: BillItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.product.icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = QuickBillPurple)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(item.color.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("${item.quantity} x ₹${item.price.toInt()}", style = MaterialTheme.typography.bodyMedium)
            }
            Text("₹${(item.price * item.quantity).toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray)
            }
        }
    }
}
