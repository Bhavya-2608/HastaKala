package com.example.hastakala

import android.app.Application
import androidx.room.Room
import com.example.hastakala.data.AppDatabase
import com.example.hastakala.repository.SaleRepository

class HastaKalaApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "hastakala_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    val repository by lazy {
        SaleRepository(database.saleDao())
    }
}
