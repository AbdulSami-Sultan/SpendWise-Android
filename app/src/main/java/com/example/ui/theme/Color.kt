package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary emerald green & teal accents for finance
val EmeraldPrimary = Color(0xFF059669)
val EmeraldOnPrimary = Color(0xFFFFFFFF)
val EmeraldPrimaryContainer = Color(0xFFD1FAE5)
val EmeraldOnPrimaryContainer = Color(0xFF064E3B)

val SlateSecondary = Color(0xFF0284C7)
val SlateOnSecondary = Color(0xFFFFFFFF)
val SlateSecondaryContainer = Color(0xFFE0F2FE)
val SlateOnSecondaryContainer = Color(0xFF0369A1)

val AmberTertiary = Color(0xFFD97706)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFEF3C7)
val AmberOnTertiaryContainer = Color(0xFF78350F)

// Dark Theme Colors
val EmeraldDarkPrimary = Color(0xFF34D399)
val EmeraldDarkOnPrimary = Color(0xFF064E3B)
val EmeraldDarkPrimaryContainer = Color(0xFF065F46)
val EmeraldDarkOnPrimaryContainer = Color(0xFFA7F3D0)

val SlateDarkSecondary = Color(0xFF38BDF8)
val SlateDarkOnSecondary = Color(0xFF082F49)
val SlateDarkSecondaryContainer = Color(0xFF0369A1)
val SlateDarkOnSecondaryContainer = Color(0xFFBAE6FD)

val AmberDarkTertiary = Color(0xFFFBBF24)
val AmberDarkOnTertiary = Color(0xFF78350F)
val AmberDarkTertiaryContainer = Color(0xFF92400E)
val AmberDarkOnTertiaryContainer = Color(0xFFFDE68A)

// Surfaces
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOutline = Color(0xFFCBD5E1)

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOutline = Color(0xFF475569)

// Category Colors for Charts
object ChartColors {
    val Food = Color(0xFF10B981)
    val Shopping = Color(0xFF6366F1)
    val Transportation = Color(0xFFF59E0B)
    val Entertainment = Color(0xFF8B5CF6)
    val Bills = Color(0xFFF43F5E)
    val Health = Color(0xFF14B8A6)
    val Groceries = Color(0xFF84CC16)
    val Travel = Color(0xFF0EA5E9)
    val Education = Color(0xFF3B82F6)
    val PersonalCare = Color(0xFFEC4899)
    val Other = Color(0xFF64748B)

    fun colorForCategory(category: String): Color {
        return when (category.trim()) {
            "Food & Dining" -> Food
            "Shopping" -> Shopping
            "Transportation" -> Transportation
            "Entertainment" -> Entertainment
            "Bills & Utilities" -> Bills
            "Health & Fitness" -> Health
            "Groceries" -> Groceries
            "Travel" -> Travel
            "Education" -> Education
            "Personal Care" -> PersonalCare
            else -> Other
        }
    }
}
