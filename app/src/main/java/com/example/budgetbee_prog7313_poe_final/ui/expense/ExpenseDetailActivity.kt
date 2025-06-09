// src/main/java/com/example/budgetbee_prog7313_poe_final/ui/expense/ExpenseDetailActivity.kt
package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.budgetbee_prog7313_poe_final.R

class ExpenseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the Action Bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail) // rename your XML to this

        val ivPhoto    = findViewById<ImageView>(R.id.imageExpense)
        val tvName     = findViewById<TextView>(R.id.textExpenseName)
        val tvAmount   = findViewById<TextView>(R.id.textExpenseAmount)
        val tvDate     = findViewById<TextView>(R.id.textExpenseDate)
        val tvCategory = findViewById<TextView>(R.id.textExpenseCategory)
        val tvDesc     = findViewById<TextView>(R.id.textExpenseDescription)
        val tvLocation = findViewById<TextView>(R.id.textExpenseLocation)
        val tvStart    = findViewById<TextView>(R.id.textExpenseStartTime)
        val tvEnd      = findViewById<TextView>(R.id.textExpenseEndTime)

        // Read extras (keys must match what you put in the Intent)
        val name        = intent.getStringExtra("name") ?: ""
        val amount      = intent.getDoubleExtra("amount", 0.0)
        val date        = intent.getStringExtra("date") ?: ""
        val category    = intent.getStringExtra("category") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val location    = intent.getStringExtra("location") ?: ""
        val startTime   = intent.getStringExtra("startTime") ?: ""
        val endTime     = intent.getStringExtra("endTime") ?: ""
        val photoUrl    = intent.getStringExtra("photoPath")

        tvName.text     = name
        tvAmount.text   = "R %.2f".format(amount)
        tvDate.text     = date
        tvCategory.text = category
        tvDesc.text     = description
        tvLocation.text = location
        tvStart.text    = startTime
        tvEnd.text      = endTime

        // Load image if present
        photoUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_expense)
                .into(ivPhoto)
        }
    }
}
