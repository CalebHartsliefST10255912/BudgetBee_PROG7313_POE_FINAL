package com.example.budgetbee_prog7313_poe_final.ui.income

import ExpenseAdapter
import IncomeAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.example.budgetbee_prog7313_poe_final.model.Income
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AllIncomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IncomeAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_income)

        FirebaseApp.initializeApp(this)

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = IncomeAdapter()
        recyclerView.adapter = adapter
        fetchIncomes()
    }


    private fun fetchIncomes() {
        val userUid = auth.currentUser?.uid

        if (userUid == null) {
            // User not logged in
            return
        }

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("incomes")
                    .whereEqualTo("userUid", userUid)
                    .get()
                    .await()

                val incomes = snapshot.toObjects(Income::class.java)
                adapter.submitList(incomes)

            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally show a user-friendly error message here
            }
        }
    }
}
