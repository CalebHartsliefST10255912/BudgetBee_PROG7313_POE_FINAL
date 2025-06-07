package com.example.budgetbee_prog7313_poe_final.model

data class Expense(
    val expenseId: Int = 0,
    val userId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val category: String = "",
    val description: String = "",
    val location: String = "",
    val photoPath: String? = null
)
