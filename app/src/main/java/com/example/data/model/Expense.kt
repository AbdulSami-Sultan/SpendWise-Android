package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val date: String = "", // YYYY-MM-DD
    val notes: String? = null,
    val paymentMethod: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "amount" to amount,
            "category" to category,
            "date" to date,
            "notes" to notes,
            "paymentMethod" to paymentMethod
        ).filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class Budget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "Overall",
    val monthlyLimit: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "category" to category,
            "monthlyLimit" to monthlyLimit
        ).filterValues { it != null }
    }
}

object ExpenseCategories {
    val ALL = listOf(
        "Food & Dining",
        "Shopping",
        "Transportation",
        "Entertainment",
        "Bills & Utilities",
        "Health & Fitness",
        "Groceries",
        "Travel",
        "Education",
        "Personal Care",
        "Other"
    )

    val PAYMENT_METHODS = listOf(
        "Credit Card",
        "Debit Card",
        "Cash",
        "Digital Wallet",
        "Bank Transfer"
    )
}
