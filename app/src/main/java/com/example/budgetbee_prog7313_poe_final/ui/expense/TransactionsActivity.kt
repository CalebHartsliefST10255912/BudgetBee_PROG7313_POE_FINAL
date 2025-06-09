// TransactionActivity.kt
package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager

class TransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the Action Bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ExpenseAdapter { expense ->
            val i = Intent(this, ExpenseDetailActivity::class.java).apply {
                putExtra("name",        expense.name)
                putExtra("amount",      expense.amount)
                putExtra("date",        expense.date)
                putExtra("category",    expense.category)
                putExtra("description", expense.description)
                putExtra("location",    expense.location)
                putExtra("startTime",   expense.startTime)
                putExtra("endTime",     expense.endTime)
                putExtra("photoPath",   expense.photoPath)
            }
            startActivity(i)
        }

        recyclerView.adapter = adapter

        FirebaseAuthManager.getCurrentUserId()?.let { uid ->
            FirestoreManager.getExpenses(uid) { list ->
                adapter.submitList(list)
            }
        }
    }
}
