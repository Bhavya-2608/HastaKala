package com.example.hastakala.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale)

    @Query("SELECT * FROM sales WHERE userEmail = :email")
    fun getAllSales(email: String): Flow<List<Sale>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun updateInventory(inventory: Inventory)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAllInventory(inventory: List<Inventory>)

    @Query("SELECT * FROM inventory WHERE userEmail = :email")
    fun getInventory(email: String): Flow<List<Inventory>>

    @Query("UPDATE inventory SET quantity = quantity - :qty WHERE item = :item AND color = :color AND userEmail = :email")
    suspend fun reduceStock(item: String, color: String, qty: Int, email: String)

    @Query("UPDATE inventory SET quantity = quantity + :qty WHERE item = :item AND color = :color AND userEmail = :email")
    suspend fun addStock(item: String, color: String, qty: Int, email: String)

    @Query("DELETE FROM inventory WHERE item = :itemName AND userEmail = :email")
    suspend fun deleteProduct(itemName: String, email: String)

    @Query("UPDATE inventory SET item = :newName WHERE item = :oldName AND userEmail = :email")
    suspend fun renameProduct(oldName: String, newName: String, email: String)

    @Query("DELETE FROM inventory WHERE item = :itemName AND color = :colorName AND userEmail = :email")
    suspend fun deleteColor(itemName: String, colorName: String, email: String)

    @Query("UPDATE inventory SET price = :newPrice WHERE item = :itemName AND userEmail = :email")
    suspend fun updateProductPrice(itemName: String, newPrice: Double, email: String)

    // User Methods
    @Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET username = :newName WHERE email = :email")
    suspend fun updateUsername(email: String, newName: String)

    @Query("DELETE FROM sales WHERE userEmail = :email")
    suspend fun clearAllSales(email: String)

    @Query("DELETE FROM inventory WHERE userEmail = :email")
    suspend fun clearAllInventory(email: String)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)
}


