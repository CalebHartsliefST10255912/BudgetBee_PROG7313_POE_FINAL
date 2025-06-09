// src/main/java/com/example/budgetbee_prog7313_poe_final/ui/income/AllIncomeActivity.kt
package com.example.budgetbee_prog7313_poe_final.ui.income

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager

class AllIncomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the Action Bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_income)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = IncomeAdapter()
        recyclerView.adapter = adapter

        FirebaseAuthManager.getCurrentUserId()?.let { uid ->
            FirestoreManager.getIncomes(uid) { incomes ->
                adapter.submitList(incomes)
            }
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
