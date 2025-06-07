package com.example.budgetbee_prog7313_poe_final.ui.category

import ExpenseAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class CategoryDetailsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: ExpenseAdapter
    private var categoryId: Int = -1
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.budgetbee_prog7313_poe_final.R.layout.activity_category_details)

        FirebaseApp.initializeApp(this)

        categoryId = intent.getIntExtra("CATEGORY_ID", -1)
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Category"

        findViewById<TextView>(R.id.textCategoryTitle).text = categoryName

        adapter = ExpenseAdapter()
        findViewById<RecyclerView>(R.id.recyclerExpenses).apply {
            layoutManager = LinearLayoutManager(this@CategoryDetailsActivity)
            adapter = this@CategoryDetailsActivity.adapter
        }

        loadExpenses()
    }

    private fun loadExpenses() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)
        if (userId == -1) {
            Log.e("CategoryDetails", "User ID not found in SharedPreferences.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Fetch expenses for category
                val expenseSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("categoryId", categoryId)
                    .get()
                    .await()

                val expenses = expenseSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)
                }

                adapter.submitList(expenses)

                val totalExpense = expenses.sumOf { it.amount }

                findViewById<TextView>(R.id.textTotalExpense).text =
                    "Total Spent: R %.2f".format(totalExpense)

                // Fetch user goal
                val goalSnapshot = firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (!goalSnapshot.isEmpty) {
                    val goal = goalSnapshot.documents[0].getDouble("maxMonthlyGoal") ?: 0.0
                    val remaining = (goal - totalExpense).coerceAtLeast(0.0)
                    val percentage = ((totalExpense / goal) * 100).toInt().coerceAtMost(100)

                    findViewById<TextView>(R.id.textTotalBalance).text =
                        "R %.2f".format(remaining)

                    findViewById<ProgressBar>(R.id.expenseProgress)?.progress = percentage
                    findViewById<TextView>(R.id.textProgressSummary)?.text =
                        "$percentage% of your goal budget used."
                } else {
                    findViewById<TextView>(R.id.textProgressSummary)?.text =
                        "Set a goal to track your spending."
                }
            } catch (e: Exception) {
                Log.e("CategoryDetails", "Error loading data", e)
            }
        }
    }
}
