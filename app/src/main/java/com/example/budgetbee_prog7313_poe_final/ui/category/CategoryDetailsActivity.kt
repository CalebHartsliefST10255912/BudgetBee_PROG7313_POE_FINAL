package com.example.budgetbee_prog7313_poe_final.ui.category

import ExpenseAdapter
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryDetailsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private var userUid: String? = null

    private lateinit var textCategoryTitle: TextView
    private lateinit var textTotalBalance: TextView
    private lateinit var textTotalExpense: TextView
    private lateinit var expenseProgress: ProgressBar
    private lateinit var textProgressSummary: TextView
    private lateinit var recyclerExpenses: RecyclerView

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textCategoryTitle = findViewById(R.id.textCategoryTitle)
        textTotalBalance = findViewById(R.id.textTotalBalance)
        textTotalExpense = findViewById(R.id.textTotalExpense)
        expenseProgress = findViewById(R.id.expenseProgress)
        textProgressSummary = findViewById(R.id.textProgressSummary)
        recyclerExpenses = findViewById(R.id.recyclerExpenses)

        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter()       // Initialize with no arguments
        recyclerExpenses.adapter = expenseAdapter

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        textCategoryTitle.text = categoryName

        loadExpenses(categoryName)
    }

    private fun loadExpenses(categoryName: String) {
        lifecycleScope.launch {
            try {
                val expensesSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userUid", userUid)
                    .whereEqualTo("category", categoryName)
                    .get()
                    .await()

                val expenses = expensesSnapshot.toObjects(Expense::class.java)

                // Calculate totals
                val totalExpense = expenses.filter { it.amount < 0 }.sumOf { it.amount }
                val totalIncome = expenses.filter { it.amount > 0 }.sumOf { it.amount }
                val totalBalance = totalIncome + totalExpense

                textTotalBalance.text = String.format("R%.2f", totalBalance)
                textTotalExpense.text = String.format("-R%.2f", -totalExpense) // expenses negative, show positive

                // Progress bar example (expense percentage)
                val maxBudget = 1000.0 // example budget
                val progressPercent = ((-totalExpense / maxBudget) * 100).toInt().coerceIn(0, 100)
                expenseProgress.progress = progressPercent
                textProgressSummary.text = "$progressPercent% of your budget. ${if (progressPercent > 80) "Careful!" else "Looks good."}"

                // Update RecyclerView using submitList()
                expenseAdapter.submitList(expenses)

            } catch (e: Exception) {
                Toast.makeText(this@CategoryDetailsActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
