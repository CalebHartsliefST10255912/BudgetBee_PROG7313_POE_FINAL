package com.example.budgetbee_prog7313_poe_final.model

data class Reward(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val cost: Int = 0,
    val currency: String = "honeyPoints",
    val imageUrl: String = "",
    val isActive: Boolean = true
)
