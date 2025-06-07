package com.example.budgetbee_prog7313_poe_final.model

data class Goal(
    val userId: String = "",
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val month: String = ""  // Format: "June 2025" or "2025-06"
)
