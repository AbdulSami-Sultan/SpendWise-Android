package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Budget
import com.example.data.model.Expense
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeFilter(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class DailySpendPoint(
    val dateString: String,
    val dayOfWeek: String,
    val totalAmount: Double
)

data class CategorySpend(
    val category: String,
    val totalAmount: Double,
    val percentage: Float
)

data class SpendingHabitsAnalysis(
    val totalSpentInPeriod: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val topCategory: String = "None",
    val topCategoryAmount: Double = 0.0,
    val highestExpense: Expense? = null,
    val transactionCount: Int = 0,
    val dailySpendPoints: List<DailySpendPoint> = emptyList(),
    val categoryBreakdowns: List<CategorySpend> = emptyList()
)

data class FilterParams(
    val filter: TimeFilter = TimeFilter.THIS_MONTH,
    val category: String? = null,
    val query: String = ""
)

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedFilter: TimeFilter = TimeFilter.THIS_MONTH,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val analysis: SpendingHabitsAnalysis = SpendingHabitsAnalysis()
)

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _filterParams = MutableStateFlow(FilterParams())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.observeExpenses(),
        repository.observeBudgets(),
        _filterParams
    ) { expenses, budgets, params ->
        val filteredExpenses = filterExpenses(expenses, params.filter, params.category, params.query)
        val analysis = calculateHabitsAnalysis(expenses, params.filter)

        ExpenseUiState(
            expenses = filteredExpenses,
            budgets = budgets,
            isLoading = false,
            errorMessage = _errorMessage.value,
            selectedFilter = params.filter,
            selectedCategory = params.category,
            searchQuery = params.query,
            analysis = analysis
        )
    }.catch { error ->
        emit(
            ExpenseUiState(
                isLoading = false,
                errorMessage = error.localizedMessage ?: "Failed to load expenses"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ExpenseUiState()
    )

    fun setTimeFilter(filter: TimeFilter) {
        _filterParams.update { it.copy(filter = filter) }
    }

    fun setCategoryFilter(category: String?) {
        _filterParams.update { it.copy(category = category) }
    }

    fun setSearchQuery(query: String) {
        _filterParams.update { it.copy(query = query) }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        date: String,
        notes: String?,
        paymentMethod: String?
    ) {
        viewModelScope.launch {
            val result = repository.addExpense(title, amount, category, date, notes, paymentMethod)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to add expense"
            }
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            val result = repository.updateExpense(expense)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to update expense"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            val result = repository.deleteExpense(expenseId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete expense"
            }
        }
    }

    fun setBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val result = repository.setBudget(category, limit)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to set budget"
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            val result = repository.deleteBudget(budgetId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete budget"
            }
        }
    }

    private fun filterExpenses(
        all: List<Expense>,
        filter: TimeFilter,
        category: String?,
        query: String
    ): List<Expense> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayCal = getMidnightCalendar()
        val todayStr = dateFormat.format(todayCal.time)

        return all.filter { exp ->
            val matchesCategory = category == null || exp.category == category
            val matchesQuery = query.isBlank() ||
                    exp.title.contains(query, ignoreCase = true) ||
                    (exp.notes?.contains(query, ignoreCase = true) == true) ||
                    exp.category.contains(query, ignoreCase = true)

            val matchesTime = when (filter) {
                TimeFilter.TODAY -> exp.date == todayStr
                TimeFilter.THIS_WEEK -> {
                    val expCal = exp.toCalendar() ?: return@filter false
                    val weekStart = getMidnightCalendar().apply {
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    }
                    !expCal.before(weekStart)
                }
                TimeFilter.THIS_MONTH -> {
                    val expCal = exp.toCalendar() ?: return@filter false
                    expCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            expCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
                }
                TimeFilter.ALL_TIME -> true
            }

            matchesCategory && matchesQuery && matchesTime
        }
    }

    private fun calculateHabitsAnalysis(all: List<Expense>, filter: TimeFilter): SpendingHabitsAnalysis {
        val inPeriod = filterExpenses(all, filter, null, "")
        if (inPeriod.isEmpty()) {
            return SpendingHabitsAnalysis(
                dailySpendPoints = generateDailyPoints(all, 7)
            )
        }

        val totalSpent = inPeriod.sumOf { it.amount }
        val categoryGroups = inPeriod.groupBy { it.category }
            .map { (cat, exps) ->
                val sum = exps.sumOf { it.amount }
                CategorySpend(
                    category = cat,
                    totalAmount = sum,
                    percentage = if (totalSpent > 0) ((sum / totalSpent) * 100).toFloat() else 0f
                )
            }.sortedByDescending { it.totalAmount }

        val topCategory = categoryGroups.firstOrNull()?.category ?: "None"
        val topCatAmount = categoryGroups.firstOrNull()?.totalAmount ?: 0.0
        val highest = inPeriod.maxByOrNull { it.amount }

        val uniqueDays = inPeriod.map { it.date }.distinct().size.coerceAtLeast(1)
        val dailyAvg = totalSpent / uniqueDays

        val dailyPoints = generateDailyPoints(all, 7)

        return SpendingHabitsAnalysis(
            totalSpentInPeriod = totalSpent,
            dailyAverage = dailyAvg,
            topCategory = topCategory,
            topCategoryAmount = topCatAmount,
            highestExpense = highest,
            transactionCount = inPeriod.size,
            dailySpendPoints = dailyPoints,
            categoryBreakdowns = categoryGroups
        )
    }

    private fun generateDailyPoints(all: List<Expense>, daysCount: Int): List<DailySpendPoint> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val points = mutableListOf<DailySpendPoint>()

        val cal = getMidnightCalendar()
        cal.add(Calendar.DAY_OF_YEAR, -(daysCount - 1))

        val expenseMap = all.groupBy { it.date }

        for (i in 0 until daysCount) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayNameFormat.format(cal.time)
            val dayTotal = expenseMap[dateStr]?.sumOf { it.amount } ?: 0.0

            points.add(DailySpendPoint(dateStr, dayName, dayTotal))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return points
    }

    private fun Expense.toCalendar(): Calendar? {
        if (date.length != 10) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(date) ?: return null
            Calendar.getInstance().apply { time = d }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMidnightCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
