package com.example.hastakala.repository

import com.example.hastakala.data.Inventory
import com.example.hastakala.data.Sale
import com.example.hastakala.data.SaleDao

class SaleRepository(private val dao: SaleDao) {
    suspend fun addSale(sale: Sale) {
        dao.insertSale(sale)
        dao.reduceStock(sale.item, sale.color, sale.quantity, sale.userEmail)
    }
    
    fun getSales(email: String) = dao.getAllSales(email)
    
    fun getInventory(email: String) = dao.getInventory(email)
    
    suspend fun updateInventory(inventory: Inventory) = dao.updateInventory(inventory)

    suspend fun insertAllInventory(inventory: List<Inventory>) = dao.insertAllInventory(inventory)

    suspend fun addStock(item: String, color: String, qty: Int, email: String) = dao.addStock(item, color, qty, email)

    suspend fun deleteProduct(itemName: String, email: String) = dao.deleteProduct(itemName, email)

    suspend fun renameProduct(oldName: String, newName: String, email: String) = dao.renameProduct(oldName, newName, email)

    suspend fun deleteColor(itemName: String, colorName: String, email: String) = dao.deleteColor(itemName, colorName, email)

    suspend fun updateProductPrice(itemName: String, newPrice: Double, email: String) = dao.updateProductPrice(itemName, newPrice, email)

    suspend fun registerUser(user: com.example.hastakala.data.User) = dao.registerUser(user)

    suspend fun getUserByEmail(email: String) = dao.getUserByEmail(email)

    suspend fun updateUsername(email: String, newName: String) = dao.updateUsername(email, newName)

    suspend fun resetAppData(email: String) {
        dao.clearAllSales(email)
        dao.clearAllInventory(email)
    }

    suspend fun deleteUser(email: String) = dao.deleteUser(email)
}
