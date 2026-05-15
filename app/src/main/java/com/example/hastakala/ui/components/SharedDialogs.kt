package com.example.hastakala.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastakala.ui.utils.ArtisanData
import com.example.hastakala.ui.utils.ColorItem
import com.example.hastakala.ui.utils.ProductItem

@Composable
fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Double) -> Unit,
    brandDark: Color,
    brandPrimary: Color
) {
    var selectedProduct by remember { mutableStateOf<ProductItem?>(null) }
    var selectedColors by remember { mutableStateOf(setOf<ColorItem>()) }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var customItemName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Inventory", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Select Product", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))
                
                // Product selection
                ArtisanData.defaultProducts.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { product ->
                            FilterChip(
                                selected = selectedProduct == product,
                                onClick = { 
                                    selectedProduct = product
                                    price = product.price.toInt().toString()
                                },
                                label = { Text(product.name) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
                FilterChip(
                    selected = selectedProduct == null && customItemName.isNotEmpty(),
                    onClick = { selectedProduct = null },
                    label = { Text("Other") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                
                if (selectedProduct == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customItemName,
                        onValueChange = { customItemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Colors", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))

                // Color selection
                ArtisanData.colors.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { colorItem ->
                            val isSelected = selectedColors.contains(colorItem)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        selectedColors = if (isSelected) {
                                            selectedColors - colorItem
                                        } else {
                                            selectedColors + colorItem
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(colorItem.color, RoundedCornerShape(6.dp))
                                            .border(0.5.dp, if (colorItem.color == Color.White) Color.LightGray else Color.Transparent, RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        colorItem.name, 
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp, 
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary, 
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val itemName = selectedProduct?.name ?: customItemName
                    val qty = quantity.toIntOrNull() ?: 0
                    val prc = price.toDoubleOrNull() ?: 0.0
                    if (itemName.isNotBlank()) {
                        if (selectedColors.isEmpty()) {
                            onConfirm(itemName, "Natural", qty, prc)
                        } else {
                            selectedColors.forEach { colorItem ->
                                onConfirm(itemName, colorItem.name, qty, prc)
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Add to Inventory", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
