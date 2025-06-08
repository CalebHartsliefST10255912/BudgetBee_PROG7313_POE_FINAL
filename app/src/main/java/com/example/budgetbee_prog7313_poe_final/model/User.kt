package com.example.budgetbee_prog7313_poe_final.model

data class User(
    val userEmail: String = "",
    val userName: String = "",
    val completedMissions: Map<String, List<String>> = mapOf()
)
