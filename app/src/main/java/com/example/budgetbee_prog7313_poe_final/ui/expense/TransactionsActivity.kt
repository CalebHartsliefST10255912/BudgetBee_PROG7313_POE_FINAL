package com.example.budgetbee_prog7313_poe_final.ui.expense

import ExpenseAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        FirebaseApp.initializeApp(this)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ExpenseAdapter()
        recyclerView.adapter = adapter

        fetchExpenses()
    }

    private fun fetchExpenses() {
        val userId = getUserId()

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val expenses = snapshot.toObjects(Expense::class.java)
                adapter.submitList(expenses)

            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally show a message to the user
            }
        }
    }

    private fun getUserId(): Int {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getInt("userId", -1)
    }
}
