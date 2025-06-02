package com.example.budgetbee_prog7313_poe_final.ui.category

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
//This is the model for the categories table
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "userId"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    val userId: Int,
    val name: String,
    val iconResId: Int
)
