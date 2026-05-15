package com.example.hastakala.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey val id: String, // e.g. "user@email.com_Bags_Blue"
    val userEmail: String,
    val item: String,
    val color: String,
    val quantity: Int,
    val price: Double = 0.0,
    val category: String = "All",
    val imageRes: Int = 0
)
