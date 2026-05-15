package com.example.hastakala.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Sale::class, Inventory::class, User::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
}
