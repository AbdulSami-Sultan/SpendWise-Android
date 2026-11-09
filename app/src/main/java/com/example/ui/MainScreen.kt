package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategories
import com.example.ui.auth.signOut
import com.example.ui.components.AddExpenseSheet
import com.example.ui.components.BudgetProgressCard
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailySpendingBarChart
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.SetBudgetDialog
import com.example.ui.components.SpendingHabitsInsightsCard
import com.example.ui.theme.ChartColors
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel,
    isGuest: Boolean = false,
    onSignOut: () -> Unit,
    onSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isAddExpenseOpen by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var isSetBudgetOpen by remember { mutableStateOf(false) }

    val user = Firebase.auth.currentUser
    val userDisplayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isGuest) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isGuest) "G" else userDisplayName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGuest) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SpendWise",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isGuest) "Guest Mode (Offline)" else "Hi, $userDisplayName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (isGuest) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onSignInClick() }
                                .padding(end = 4.dp)
                                .testTag("top_sign_in_button")
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (isGuest) {
                                onSignOut()
                            } else {
                                signOut(context, credentialManager, onSignOut, coroutineScope)
                            }
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = if (isGuest) "Exit Guest Mode" else "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Transactions") },
                    label = { Text("Expenses") },
                    modifier = Modifier.testTag("tab_expenses")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Budgets") },
                    label = { Text("Budgets") },
                    modifier = Modifier.testTag("tab_budgets")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingExpense = null
                    isAddExpenseOpen = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Expense") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_expense_fab")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (selectedTab) {
                    0 -> DashboardTab(
                        uiState = uiState,
                        isGuest = isGuest,
                        onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                        onEditExpense = {
                            editingExpense = it
                            isAddExpenseOpen = true
                        },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onViewAllTransactions = { selectedTab = 1 },
                        onSignInClick = onSignInClick,
                        onAddExpenseClick = {
                            editingExpense = null
                            isAddExpenseOpen = true
                        }
                    )
                    1 -> TransactionsTab(
                        uiState = uiState,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setCategoryFilter(it) },
                        onEditExpense = {
                            editingExpense = it
                            isAddExpenseOpen = true
                        },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                    2 -> BudgetsTab(
                        uiState = uiState,
                        onOpenSetBudget = { isSetBudgetOpen = true },
                        onDeleteBudget = { viewModel.deleteBudget(it) }
                    )
                }
            }
        }
    }

    if (isAddExpenseOpen) {
        AddExpenseSheet(
            existingExpense = editingExpense,
            onDismiss = {
                isAddExpenseOpen = false
                editingExpense = null
            },
            onSaveExpense = { title, amount, category, date, notes, method ->
                if (editingExpense == null) {
                    viewModel.addExpense(title, amount, category, date, notes, method)
                } else {
                    viewModel.updateExpense(
                        editingExpense!!.copy(
                            title = title,
                            amount = amount,
                            category = category,
                            date = date,
                            notes = notes,
                            paymentMethod = method
                        )
                    )
                }
            }
        )
    }

    if (isSetBudgetOpen) {
        SetBudgetDialog(
            onDismiss = { isSetBudgetOpen = false },
            onSaveBudget = { category, limit ->
                viewModel.setBudget(category, limit)
            }
        )
    }
}

@Composable
private fun DashboardTab(
    uiState: ExpenseUiState,
    isGuest: Boolean = false,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onViewAllTransactions: () -> Unit,
    onSignInClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isGuest) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_sync_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Mode Active",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Your data is stored locally. Sign in with Google to backup and sync across devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onSignInClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("banner_sign_in_button")
                        ) {
                            Text("Sign In")
                        }
                    }
                }
            }
        }

        // Time Period Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TimeFilter.values()) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { onTimeFilterSelected(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Current Balance Card
        item {
            val totalBudget = if (uiState.budgets.isNotEmpty()) {
                uiState.budgets.sumOf { it.monthlyLimit }
            } else {
                3000.0 // Default reference target budget
            }
            val totalSpent = uiState.analysis.totalSpentInPeriod
            val currentBalance = totalBudget - totalSpent
            val isPositive = currentBalance >= 0.0
            val progress = if (totalBudget > 0.0) {
                (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
            } else 0f
            val percentRemaining = if (totalBudget > 0.0) {
                (((totalBudget - totalSpent) / totalBudget) * 100).toInt().coerceAtLeast(0)
            } else 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_balance_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Row: Wallet icon, "Current Balance", and Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Current Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Remaining in ${uiState.selectedFilter.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPositive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = if (isPositive) "$percentRemaining% Left" else "Over Budget",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Balance Text
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", currentBalance)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("current_balance_text")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comparison row: Total Spent & Target Budget
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "Spent",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Total Spent",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", totalSpent)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Budget",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Target Budget",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", totalBudget)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recent_transactions_section"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.expenses.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${uiState.expenses.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    TextButton(
                        onClick = { onViewAllTransactions() },
                        modifier = Modifier.testTag("view_all_transactions_button")
                    ) {
                        Text(
                            text = "View All",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (uiState.expenses.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No recent transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add an expense to start tracking your daily spending habits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                            )
                            Button(
                                onClick = onAddExpenseClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("empty_add_expense_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Expense")
                            }
                        }
                    }
                } else {
                    uiState.expenses.take(5).forEach { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onEdit = onEditExpense,
                            onDelete = onDeleteExpense
                        )
                    }
                }
            }
        }

        // Daily Trend Bar Chart
        item {
            DailySpendingBarChart(points = uiState.analysis.dailySpendPoints)
        }

        // Category Donut Chart
        item {
            CategoryDonutChart(
                breakdowns = uiState.analysis.categoryBreakdowns,
                totalAmount = uiState.analysis.totalSpentInPeriod
            )
        }

        // Spending Habits Insights
        item {
            SpendingHabitsInsightsCard(analysis = uiState.analysis)
        }
    }
}

@Composable
private fun TransactionsTab(
    uiState: ExpenseUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by merchant, note, or category...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_expenses_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("All Categories") }
                )
            }
            items(ExpenseCategories.ALL) { cat ->
                val isSelected = uiState.selectedCategory == cat
                val catColor = ChartColors.colorForCategory(cat)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(if (isSelected) null else cat) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grouped Transactions List
        if (uiState.expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try adjusting your search or filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            val grouped = remember(uiState.expenses) {
                uiState.expenses.groupBy { it.date }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (dateStr, itemsForDate) ->
                    val daySum = itemsForDate.sumOf { it.amount }
                    item(key = "header_$dateStr") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDisplayDate(dateStr),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Day Total: $${String.format(Locale.US, "%.2f", daySum)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(itemsForDate, key = { it.id }) { expense ->
                        ExpenseItemCard(
                            expense = expense,
                            onEdit = onEditExpense,
                            onDelete = onDeleteExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetsTab(
    uiState: ExpenseUiState,
    onOpenSetBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    val thisMonth = remember {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        sdf.format(Date())
    }

    // Calculate this month's spending by category
    val currentMonthExpenses = remember(uiState.expenses, thisMonth) {
        uiState.expenses.filter { it.date.startsWith(thisMonth) }
    }
    val spentByCategory = remember(currentMonthExpenses) {
        currentMonthExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set spending targets to build healthier habits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onOpenSetBudget() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Budget",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (uiState.budgets.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No budgets set yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track your food, shopping, or entertainment budget to avoid overspending.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onOpenSetBudget() }
                        ) {
                            Text(
                                text = "Create Category Budget",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(uiState.budgets, key = { it.id }) { budget ->
                val spent = spentByCategory[budget.category] ?: 0.0
                BudgetProgressCard(
                    budget = budget,
                    spentThisMonth = spent,
                    onDelete = onDeleteBudget
                )
            }
        }

        // Overall Spending Habits Summary
        item {
            SpendingHabitsInsightsCard(analysis = uiState.analysis)
        }
    }
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val todayStr = sdf.format(Date())
        if (dateStr == todayStr) return "Today"

        val outFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        outFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}
