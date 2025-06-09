package com.example.budgetbee_prog7313_poe_final.ui.category

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.example.budgetbee_prog7313_poe_final.ui.expense.ExpenseAdapter
import com.example.budgetbee_prog7313_poe_final.ui.expense.ExpenseDetailActivity
import kotlinx.coroutines.launch

class CategoryDetailsActivity : AppCompatActivity() {

    private var userUid: String = ""

    private lateinit var textCategoryTitle: TextView
    private lateinit var textTotalBalance: TextView
    private lateinit var textTotalExpense: TextView
    private lateinit var expenseProgress: ProgressBar
    private lateinit var textProgressSummary: TextView
    private lateinit var recyclerExpenses: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the Action Bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // Auth check
        FirebaseAuthManager.getCurrentUserId()?.let {
            userUid = it
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get the category we’re displaying
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""

        // Bind views
        textCategoryTitle   = findViewById(R.id.textCategoryTitle)
        textTotalBalance    = findViewById(R.id.textTotalBalance)
        textTotalExpense    = findViewById(R.id.textTotalExpense)
        expenseProgress     = findViewById(R.id.expenseProgress)
        textProgressSummary = findViewById(R.id.textProgressSummary)
        recyclerExpenses    = findViewById(R.id.recyclerExpenses)

        // Setup RecyclerView + adapter with click handler
        recyclerExpenses.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter { expense ->
            // Launch detail screen with all fields passed as extras
            val intent = Intent(this, ExpenseDetailActivity::class.java).apply {
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
            startActivity(intent)
        }
        recyclerExpenses.adapter = expenseAdapter

        textCategoryTitle.text = categoryName

        // Load and display all expenses for this category
        loadExpenses(categoryName)
    }

    private fun loadExpenses(categoryName: String) {
        lifecycleScope.launch {
            FirestoreManager.getExpenses(userUid) { expenses ->
                val filtered = expenses.filter { it.category == categoryName }

                // Totals
                val totalExpense = filtered.filter { it.amount < 0 }.sumOf { it.amount }
                val totalIncome  = filtered.filter { it.amount > 0 }.sumOf { it.amount }
                val totalBalance = totalIncome + totalExpense

                textTotalBalance.text = "R%.2f".format(totalBalance)
                textTotalExpense.text = "-R%.2f".format(-totalExpense)

                // Progress (example budget of 1000)
                val progressPercent = ((-totalExpense / 1000.0) * 100).toInt().coerceIn(0, 100)
                expenseProgress.progress = progressPercent
                textProgressSummary.text =
                    "$progressPercent% of your budget. ${if (progressPercent > 80) "Careful!" else "Looks good."}"

                // Update list
                expenseAdapter.submitList(filtered)
            }
        }
    }
}
