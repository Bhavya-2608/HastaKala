package com.example.hastakala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.hastakala.ui.*
import com.example.hastakala.ui.theme.HastaKalaTheme
import com.example.hastakala.viewmodel.SaleViewModel
import com.example.hastakala.viewmodel.SaleViewModelFactory
import com.example.hastakala.viewmodel.AuthViewModel
import com.example.hastakala.viewmodel.AuthViewModelFactory
import com.example.hastakala.data.AuthManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authManager = AuthManager(this)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            HastaKalaTheme(darkTheme = isDarkTheme) {
                val app = application as HastaKalaApplication
                val saleViewModel: SaleViewModel = viewModel(
                    factory = SaleViewModelFactory(app.repository, authManager),
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authManager, app.repository)
                )
                MainScreen(saleViewModel, authViewModel, isDarkTheme, onThemeChange = { isDarkTheme = it })
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Default.Home)
    object Auth : Screen("auth", "Authentication", Icons.Default.Settings) // Use Settings temporarily as Person might be missing
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object QuickBill : Screen("quickbill", "Quick Bill", Icons.Default.ShoppingCart)
    object Inventory : Screen("inventory", "Inventory", Icons.Default.List)
    object Restock : Screen("restock", "Restock", Icons.Default.AddCircle)
    object Income : Screen("income", "Income Log", Icons.Default.ShoppingCart) // Use ShoppingCart since Payments is missing
    object IncomeTrend : Screen("income_trend", "Income Trend", Icons.Default.ShoppingCart)
    object SalesAnalytics : Screen("sales_analytics", "Sales Analytics", Icons.Default.Star)
    object TransactionReport : Screen("transaction_report", "Transaction Report", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    saleViewModel: SaleViewModel,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val username by authViewModel.username.collectAsState()
    val email by authViewModel.userEmail.collectAsState()
    val focusManager = LocalFocusManager.current

    val menuItems = listOf(
        Screen.Dashboard,
        Screen.QuickBill,
        Screen.Inventory,
        Screen.Restock,
        Screen.Income,
        Screen.Settings
    )

    val showBars = currentRoute != Screen.Welcome.route && 
                   currentRoute != Screen.Auth.route && 
                   currentRoute != Screen.IncomeTrend.route && 
                   currentRoute != Screen.SalesAnalytics.route

    val billItems by saleViewModel.billItems.collectAsState()

    // Handle logout state change
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != Screen.Welcome.route && currentRoute != Screen.Auth.route) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBars,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(320.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight().padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hasta-Kala",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    menuItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationDrawerItem(
                            label = { Text(screen.label, style = MaterialTheme.typography.labelLarge) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                scope.launch { drawerState.close() }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    
                    NavigationDrawerItem(
                        label = { Text("Logout", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) },
                        selected = false,
                        onClick = {
                            authViewModel.logout()
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) {
    Scaffold(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
            topBar = {
                if (showBars) {
                    val isTopLevel = menuItems.any { it.route == currentRoute }
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                if (currentRoute == Screen.Dashboard.route) "Hasta-Kala" else when (currentRoute) {
                                    Screen.QuickBill.route -> "Quick Bill"
                                    Screen.Inventory.route -> "Inventory"
                                    Screen.Restock.route -> "Inventory Restock"
                                    Screen.Income.route -> "Income Log"
                                    Screen.Settings.route -> "Settings"
                                    Screen.TransactionReport.route -> "Transaction Records"
                                    else -> "Hasta-Kala"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFF5D3587) // QuickBillPurple / Deep Purple
                        ),
                        navigationIcon = {
                            if (currentRoute == Screen.Dashboard.route) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                                }
                            } else if (currentRoute != null) {
                                IconButton(onClick = { 
                                    if (currentRoute == Screen.TransactionReport.route) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                // Reset search and UI state as a "refresh"
                                saleViewModel.updateSearchQuery("")
                                saleViewModel.updateCategory("All")
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                            if (currentRoute == Screen.QuickBill.route) {
                                BadgedBox(
                                    badge = { if (billItems.isNotEmpty()) Badge { Text("${billItems.size}") } },
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                                }
                            } else {
                                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                    if (username.isNotEmpty()) {
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.2f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = username.take(1).uppercase(),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = "Settings", tint = Color.White)
                                    }
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == Screen.Dashboard.route) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.QuickBill.route) },
                        containerColor = Color(0xFF5D3587),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Quick Bill")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Welcome.route,
                modifier = Modifier.padding(if (showBars) innerPadding else PaddingValues(0.dp)),
            ) {
                composable(Screen.Welcome.route) { 
                    WelcomeScreen(onNavigateToAuth = { navController.navigate(Screen.Auth.route) }) 
                }
                composable(Screen.Auth.route) { 
                    AuthScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = { 
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    ) 
                }
                composable(Screen.Dashboard.route) { 
                    DashboardScreen(
                        viewModel = saleViewModel,
                        username = username,
                        onNavigateToIncomeTrend = { navController.navigate(Screen.IncomeTrend.route) },
                        onNavigateToSalesAnalytics = { navController.navigate(Screen.SalesAnalytics.route) },
                        onNavigateToQuickBill = { navController.navigate(Screen.QuickBill.route) },
                        onNavigateToRestock = { navController.navigate(Screen.Restock.route) }
                    ) 
                }
                composable(Screen.IncomeTrend.route) {
                    IncomeTrendScreen(
                        viewModel = saleViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.SalesAnalytics.route) {
                    SalesAnalyticsScreen(
                        viewModel = saleViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.TransactionReport.route) {
                    TransactionReportScreen(
                        viewModel = saleViewModel,
                        userEmail = email,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.QuickBill.route) { 
                    QuickBillScreen(
                        viewModel = saleViewModel,
                        onNavigateToInventory = { 
                            navController.navigate(Screen.Inventory.route)
                        },
                        onBack = { navController.popBackStack() }
                    ) 
                }
                composable(Screen.Inventory.route) { InventoryScreen(saleViewModel) }
                composable(Screen.Restock.route) { RestockScreen(saleViewModel) }
                composable(Screen.Income.route) { 
                    IncomeLogScreen(
                        viewModel = saleViewModel,
                        onNavigateToTransactionReport = { 
                            navController.navigate(Screen.TransactionReport.route)
                        }
                    ) 
                }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    isDarkTheme = isDarkTheme, 
                    onThemeChange = onThemeChange,
                    username = username,
                    email = email,
                    onLogout = {
                        authViewModel.logout()
                    },
                    onResetData = {
                        saleViewModel.resetAllData()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onDeleteAccount = {
                        authViewModel.deleteAccount()
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onUpdateUsername = { newName ->
                        authViewModel.updateUsername(newName)
                    }
                ) 
            }
        }
        }
    }
}
