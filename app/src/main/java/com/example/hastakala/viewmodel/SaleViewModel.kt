package com.example.hastakala.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hastakala.data.Inventory
import com.example.hastakala.data.Sale
import com.example.hastakala.data.AuthManager
import com.example.hastakala.repository.SaleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.hastakala.ui.utils.BillItem
import com.example.hastakala.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar
import java.util.SortedMap

data class SalesStats(
    val bestSellers: List<Pair<String, Int>> = emptyList(),
    val colorDistribution: Map<String, Int> = emptyMap(),
    val productRevenue: List<Pair<String, Double>> = emptyList(),
    val dayProductRevenue: List<Pair<String, Double>> = emptyList(),
    val weekProductRevenue: List<Pair<String, Double>> = emptyList(),
    val monthProductRevenue: List<Pair<String, Double>> = emptyList(),
    val dailyIncome: Map<String, Double> = emptyMap(),
    val weeklyIncome: Map<String, Double> = emptyMap(),
    val monthlyIncome: Map<String, Double> = emptyMap(),
    val weekBestSellers: List<Triple<String, Int, Double>> = emptyList(),
    val monthBestSellers: List<Triple<String, Int, Double>> = emptyList(),
    val todaySales: Double = 0.0,
    val yesterdaySales: Double = 0.0,
    val percentageChange: Int = 0,
    val monthTotalIncome: Double = 0.0,
    val monthTotalOrders: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModel(
    private val repository: SaleRepository,
    authManager: AuthManager
) : ViewModel() {

    val userEmail = authManager.userEmailFlow

    val sales = userEmail.flatMapLatest { email ->
        if (email.isEmpty()) flowOf(emptyList())
        else repository.getSales(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory = userEmail.flatMapLatest { email ->
        if (email.isEmpty()) flowOf(emptyList())
        else repository.getInventory(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _billItems = MutableStateFlow<List<BillItem>>(emptyList())
    val billItems = _billItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredInventory = combine(inventory, _searchQuery, _selectedCategory) { list, query, category ->
        list.filter {
            val matchesSearch = it.item.contains(query, true)
            val matchesCategory = category == "All" || it.category == category
            matchesSearch && matchesCategory
        }.groupBy { it.item }.toSortedMap()
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap<String, List<Inventory>>().toSortedMap())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    val stats = sales.map { saleList ->
        if (saleList.isEmpty()) return@map SalesStats()

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val monthStart = calendar.timeInMillis

        val weekAgo = now - (7L * 24 * 60 * 60 * 1000)
        val dayAgo = now - (24L * 60 * 60 * 1000)

        val todaySales = saleList.filter { it.date >= todayStart }.sumOf { it.price * it.quantity }
        val yesterdaySales = saleList.filter { it.date >= yesterdayStart && it.date < todayStart }.sumOf { it.price * it.quantity }
        
        val percentageChange = if (yesterdaySales > 0) {
            ((todaySales - yesterdaySales) / yesterdaySales * 100).toInt()
        } else if (todaySales > 0) {
            100
        } else {
            0
        }

        val currentMonthSales = saleList.filter { it.date >= monthStart }
        val monthTotalIncome = currentMonthSales.sumOf { it.price * it.quantity }
        val monthTotalOrders = currentMonthSales.size

        SalesStats(
            bestSellers = saleList.groupBy { "${it.item} (${it.color})" }
                .mapValues { it.value.sumOf { s -> s.quantity } }
                .toList()
                .sortedByDescending { it.second }
                .take(5),

            colorDistribution = saleList.groupBy { it.color }
                .mapValues { it.value.sumOf { s -> s.quantity } },

            productRevenue = saleList.groupBy { it.item }
                .mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                .toList()
                .sortedByDescending { it.second },

            dayProductRevenue = saleList.filter { it.date >= dayAgo }
                .groupBy { it.item }
                .mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                .toList()
                .sortedByDescending { it.second },

            weekProductRevenue = saleList.filter { it.date >= weekAgo }
                .groupBy { it.item }
                .mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                .toList()
                .sortedByDescending { it.second },

            monthProductRevenue = currentMonthSales
                .groupBy { it.item }
                .mapValues { it.value.sumOf { s -> s.price * s.quantity } }
                .toList()
                .sortedByDescending { it.second },

            dailyIncome = saleList.groupBy { sale ->
                calendar.timeInMillis = sale.date
                "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}"
            }.mapValues { it.value.sumOf { s -> s.price * s.quantity } },

            weeklyIncome = saleList.groupBy { sale ->
                calendar.timeInMillis = sale.date
                "W${calendar.get(Calendar.WEEK_OF_YEAR)}"
            }.mapValues { it.value.sumOf { s -> s.price * s.quantity } },

            monthlyIncome = saleList.groupBy { sale ->
                calendar.timeInMillis = sale.date
                "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            }.mapValues { it.value.sumOf { s -> s.price * s.quantity } },

            weekBestSellers = saleList.filter { it.date >= weekAgo }
                .groupBy { it.item }
                .mapValues { entry -> 
                    val count = entry.value.sumOf { it.quantity }
                    val avgPrice = if (entry.value.isNotEmpty()) entry.value.sumOf { it.price } / entry.value.size else 0.0
                    Pair(count, avgPrice)
                }
                .toList()
                .map { (name, s) -> Triple(name, s.first, s.second) }
                .sortedByDescending { it.second }
                .take(3),

            monthBestSellers = currentMonthSales
                .groupBy { it.item }
                .mapValues { entry -> 
                    val count = entry.value.sumOf { it.quantity }
                    val avgPrice = if (entry.value.isNotEmpty()) entry.value.sumOf { it.price } / entry.value.size else 0.0
                    Pair(count, avgPrice)
                }
                .toList()
                .map { (name, s) -> Triple(name, s.first, s.second) }
                .sortedByDescending { it.second }
                .take(3),
            
            todaySales = todaySales,
            yesterdaySales = yesterdaySales,
            percentageChange = percentageChange,
            monthTotalIncome = monthTotalIncome,
            monthTotalOrders = monthTotalOrders
        )
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SalesStats())

    val weekBestSellers = stats.map { it.weekBestSellers }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val monthBestSellers = stats.map { it.monthBestSellers }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToBill(item: BillItem) {
        _billItems.value = _billItems.value + item
    }

    fun removeFromBill(item: BillItem) {
        _billItems.value = _billItems.value.filter { it != item }
    }

    fun clearBill() {
        _billItems.value = emptyList()
    }

    fun addSale(item: String, color: String, price: Double, quantity: Int) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.addSale(
                Sale(
                    userEmail = email,
                    item = item,
                    color = color,
                    price = price,
                    quantity = quantity,
                    date = System.currentTimeMillis()
                )
            )
        }
    }

    fun restock(
        item: String,
        color: String,
        qty: Int,
        price: Double = 0.0,
        category: String = "All",
        imageRes: Int = R.drawable.ic_launcher_background
    ) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            val currentInventory = repository.getInventory(email).first()
            val existingItem = currentInventory.find { it.item == item && it.color == color }
            
            if (existingItem != null) {
                repository.addStock(item, color, qty, email)
            } else {
                val newId = "${email}_${item}_${color}"
                repository.updateInventory(Inventory(newId, email, item, color, qty, price, category, imageRes))
            }
        }
    }

    fun deleteProduct(itemName: String) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.deleteProduct(itemName, email)
        }
    }

    fun deleteColor(itemName: String, colorName: String) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.deleteColor(itemName, colorName, email)
        }
    }

    fun renameProduct(oldName: String, newName: String) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.renameProduct(oldName, newName, email)
        }
    }

    fun updatePrice(itemName: String, newPrice: Double) {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.updateProductPrice(itemName, newPrice, email)
        }
    }

    fun resetAllData() {
        val email = userEmail.value
        if (email.isEmpty()) return
        viewModelScope.launch {
            repository.resetAppData(email)
            clearBill()
            _searchQuery.value = ""
            _selectedCategory.value = "All"
        }
    }
}

class SaleViewModelFactory(
    private val repository: SaleRepository,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SaleViewModel(repository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
