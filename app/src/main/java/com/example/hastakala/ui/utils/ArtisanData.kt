package com.example.hastakala.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ProductItem(val name: String, val icon: ImageVector, val price: Double)
data class ColorItem(val name: String, val color: Color)
data class BillItem(
    val product: ProductItem,
    val color: ColorItem,
    val quantity: Int,
    val price: Double
)

object ArtisanData {
    val defaultProducts = listOf(
        ProductItem("Banana Fiber Bag", Icons.Default.ShoppingCart, 350.0),
        ProductItem("Keychain", Icons.Default.Favorite, 50.0),
        ProductItem("Pouch", Icons.Default.Star, 150.0),
        ProductItem("Wall Hanging", Icons.Default.Home, 800.0),
    )

    val colors = listOf(
        ColorItem("Red", Color(0xFFD32F2F)),
        ColorItem("Blue", Color(0xFF1976D2)),
        ColorItem("Green", Color(0xFF388E3C)),
        ColorItem("Yellow", Color(0xFFFBC02D)),
        ColorItem("Natural", Color(0xFF8D6E63)),
        ColorItem("Black", Color(0xFF212121)),
        ColorItem("White", Color(0xFFFFFFFF)),
        ColorItem("Orange", Color(0xFFFF9800)),
        ColorItem("Pink", Color(0xFFE91E63)),
        ColorItem("Purple", Color(0xFF9C27B0)),
        ColorItem("Brown", Color(0xFF795548)),
        ColorItem("Maroon", Color(0xFF800000)),
        ColorItem("Teal", Color(0xFF008080)),
        ColorItem("Cyan", Color(0xFF00BCD4)),
        ColorItem("Gray", Color(0xFF9E9E9E)),
        ColorItem("Turquoise", Color(0xFF40E0D0)),
        ColorItem("Gold", Color(0xFFFFD700)),
        ColorItem("Silver", Color(0xFFC0C0C0)),
        ColorItem("Beige", Color(0xFFF5F5DC)),
        ColorItem("Lavender", Color(0xFFE6E6FA)),
        ColorItem("Terracotta", Color(0xFFE2725B)),
        ColorItem("Indigo", Color(0xFF4B0082)),
        ColorItem("Mustard", Color(0xFFFFDB58)),
        ColorItem("Olive", Color(0xFF808000)),
        ColorItem("Navy", Color(0xFF000080)),
        ColorItem("Charcoal", Color(0xFF36454F)),
        ColorItem("Cream", Color(0xFFFFFDD0)),
        ColorItem("Peach", Color(0xFFFFE5B4)),
        ColorItem("Multicolor", Color(0xFF000000)),
    )
}
