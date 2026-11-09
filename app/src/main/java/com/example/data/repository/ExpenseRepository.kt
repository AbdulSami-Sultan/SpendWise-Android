package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetDao
import com.example.data.local.ExpenseDao
import com.example.data.local.LocalBudget
import com.example.data.model.Budget
import com.example.data.model.Expense
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val db: FirebaseFirestore? = null,
    private val expenseDao: ExpenseDao? = null,
    private val budgetDao: BudgetDao? = null,
    val isGuest: Boolean = false
) {

    // 1. Existing Firestore constructor: For rule tests & cloud callers
    constructor(db: FirebaseFirestore) : this(
        db = db,
        expenseDao = null,
        budgetDao = null,
        isGuest = false
    )

    // 2. Context constructor: Supports guest mode and signed-in cloud mode
    constructor(context: Context, isGuest: Boolean = false) : this(
        db = if (isGuest) null else FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        expenseDao = AppDatabase.getInstance(context).expenseDao(),
        budgetDao = AppDatabase.getInstance(context).budgetDao(),
        isGuest = isGuest
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeExpenses(): Flow<List<Expense>> = flow {
        if (isGuest && expenseDao != null) {
            val flow = expenseDao.getAllExpenses().map { localList ->
                localList.map { local ->
                    Expense(
                        id = local.id.toString(),
                        userId = "guest",
                        title = local.description,
                        amount = local.amount,
                        category = local.category,
                        date = local.date
                    )
                }
            }
            emitAll(flow)
            return@flow
        }

        val uid = requireUserId()
        val path = "users/$uid/expenses"
        val query = checkNotNull(db) { "Firestore instance is required for cloud mode" }
            .collection("users").document(uid).collection("expenses")

        val flow = query.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Expense::class.java).sortedByDescending { it.date }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }

        emitAll(flow)
    }

    suspend fun addExpense(
        title: String,
        amount: Double,
        category: String,
        date: String,
        notes: String? = null,
        paymentMethod: String? = null
    ): Result<String> {
        if (isGuest && expenseDao != null) {
            return try {
                val localExpense = com.example.data.local.Expense(
                    amount = amount,
                    category = category.trim(),
                    date = date.trim(),
                    description = title.trim()
                )
                val id = expenseDao.insertExpense(localExpense)
                Result.success(id.toString())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            val uid = requireUserId()
            val expensesRef = checkNotNull(db) { "Firestore instance is required for cloud mode" }
                .collection("users").document(uid).collection("expenses")

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "title" to title.trim(),
                "amount" to amount,
                "category" to category.trim(),
                "date" to date.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!notes.isNullOrBlank()) {
                payload["notes"] = notes.trim()
            }
            if (!paymentMethod.isNullOrBlank()) {
                payload["paymentMethod"] = paymentMethod.trim()
            }

            val doc = expensesRef.add(payload).await()
            Result.success(doc.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/expenses")
            Result.failure(e)
        }
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> {
        if (isGuest && expenseDao != null) {
            return try {
                val idLong = expense.id.toLongOrNull() ?: 0L
                val localExpense = com.example.data.local.Expense(
                    id = idLong,
                    amount = expense.amount,
                    category = expense.category,
                    date = expense.date,
                    description = expense.title
                )
                expenseDao.updateExpense(localExpense)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            val uid = requireUserId()
            require(expense.id.isNotBlank()) { "Expense ID cannot be blank" }
            val docRef = checkNotNull(db) { "Firestore instance is required for cloud mode" }
                .collection("users").document(uid).collection("expenses").document(expense.id)

            val payload = expense.toMap().toMutableMap()
            payload["userId"] = uid
            payload["updatedAt"] = FieldValue.serverTimestamp()

            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/${auth.currentUser?.uid}/expenses/${expense.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        if (isGuest && expenseDao != null) {
            return try {
                val idLong = expenseId.toLongOrNull() ?: 0L
                expenseDao.deleteExpenseById(idLong)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            val uid = requireUserId()
            require(expenseId.isNotBlank()) { "Expense ID cannot be blank" }
            val docRef = checkNotNull(db) { "Firestore instance is required for cloud mode" }
                .collection("users").document(uid).collection("expenses").document(expenseId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/expenses/$expenseId")
            Result.failure(e)
        }
    }

    fun observeBudgets(): Flow<List<Budget>> = flow {
        if (isGuest && budgetDao != null) {
            val flow = budgetDao.getAllBudgets().map { localList ->
                localList.map { local ->
                    Budget(
                        id = local.category,
                        userId = "guest",
                        category = local.category,
                        monthlyLimit = local.monthlyLimit
                    )
                }
            }
            emitAll(flow)
            return@flow
        }

        val uid = requireUserId()
        val path = "users/$uid/budgets"
        val query = checkNotNull(db) { "Firestore instance is required for cloud mode" }
            .collection("users").document(uid).collection("budgets")

        val flow = query.snapshots()
            .map { snapshot -> snapshot.toObjects(Budget::class.java) }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }

        emitAll(flow)
    }

    suspend fun setBudget(category: String, monthlyLimit: Double): Result<String> {
        if (isGuest && budgetDao != null) {
            return try {
                budgetDao.setBudget(LocalBudget(category = category, monthlyLimit = monthlyLimit))
                Result.success(category)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            val uid = requireUserId()
            val budgetsRef = checkNotNull(db) { "Firestore instance is required for cloud mode" }
                .collection("users").document(uid).collection("budgets")
            val budgetId = category.lowercase().replace("[^a-z0-9]".toRegex(), "_")
            val docRef = budgetsRef.document(budgetId)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val updatePayload = mapOf(
                    "category" to category,
                    "monthlyLimit" to monthlyLimit,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "category" to category,
                    "monthlyLimit" to monthlyLimit,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(budgetId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/budgets")
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(budgetId: String): Result<Unit> {
        if (isGuest && budgetDao != null) {
            return try {
                budgetDao.deleteBudgetByCategory(budgetId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        return try {
            val uid = requireUserId()
            val docRef = checkNotNull(db) { "Firestore instance is required for cloud mode" }
                .collection("users").document(uid).collection("budgets").document(budgetId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/budgets/$budgetId")
            Result.failure(e)
        }
    }

    private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(flow: Flow<T>) {
        flow.collect { value -> emit(value) }
    }
}
