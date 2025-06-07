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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        val userUid = auth.currentUser?.uid

        if (userUid == null) {
            // User not logged in
            return
        }

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("expenses")
                    .whereEqualTo("userUid", userUid)
                    .get()
                    .await()

                val expenses = snapshot.toObjects(Expense::class.java)
                adapter.submitList(expenses)

            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally show a user-friendly error message here
            }
        }
    }
}
