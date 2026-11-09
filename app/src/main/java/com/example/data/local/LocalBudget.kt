package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_budgets")
data class LocalBudget(
    @PrimaryKey
    val category: String,
    val monthlyLimit: Double
)

@Dao
interface BudgetDao {
    @Query("SELECT * FROM local_budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<LocalBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: LocalBudget)

    @Query("DELETE FROM local_budgets WHERE category = :category")
    suspend fun deleteBudgetByCategory(category: String)
}
