package com.example.hastakala.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val username: String,
    val password: Int // In a real app, use hashed passwords. Storing hashcode for simplicity here.
)
