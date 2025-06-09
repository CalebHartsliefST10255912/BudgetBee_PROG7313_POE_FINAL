package com.example.budgetbee_prog7313_poe_final.model

data class Reward(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var cost: Int = 0,
    var currency: String = "honeyPoints",
    var imageUrl: String = "",
    var isActive: Boolean = true
)

