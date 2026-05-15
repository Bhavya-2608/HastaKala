package com.example.hastakala.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val item: String,
    val color: String,
    val price: Double,
    val quantity: Int,
    val date: Long
)