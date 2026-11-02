package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addExpense_validPayload_createsDocumentAndReturnsId() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ExpenseRepository(firestore)

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addExpense(
        title = "Coffee",
        amount = 4.5,
        category = "Food & Dining",
        date = "2026-11-02",
        notes = "Espresso",
        paymentMethod = "Credit Card"
      )
    }
    assertTrue(createResult.isSuccess)
    val id = createResult.getOrThrow()
    assertTrue(id.isNotEmpty())
  }

  @Test
  fun observeExpenses_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ExpenseRepository(firestore)

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addExpense(
        title = "Groceries",
        amount = 54.2,
        category = "Groceries",
        date = "2026-11-02"
      )
    }
    val expenseId = createResult.getOrThrow()

    val emitted = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeExpenses().first { list -> list.any { it.id == expenseId } }
    }
    assertTrue(emitted.any { it.id == expenseId })
  }

  @Test
  fun observeExpenses_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()
    val repository = ExpenseRepository(firestore)

    var caught = false
    try {
      withTimeout(FLOW_TIMEOUT_MS) { repository.observeExpenses().first() }
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      caught = e is IllegalStateException || firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    assertTrue(caught)
  }

  @Test
  fun setBudget_andObserveBudgets_authenticatedOwner_succeeds() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = ExpenseRepository(firestore)

    val budgetResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.setBudget("Food & Dining", 400.0)
    }
    assertTrue(budgetResult.isSuccess)

    val budgets = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeBudgets().first { list -> list.any { it.category == "Food & Dining" } }
    }
    assertTrue(budgets.any { it.monthlyLimit == 400.0 })
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 5000L
  }
}
