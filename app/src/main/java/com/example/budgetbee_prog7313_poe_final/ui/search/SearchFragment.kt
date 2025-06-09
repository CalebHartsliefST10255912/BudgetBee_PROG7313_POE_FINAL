package com.example.budgetbee_prog7313_poe_final.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
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
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var categorySpinner: Spinner
    private lateinit var dateSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var noItemsTextView: TextView
    private lateinit var adapter: ExpenseAdapter

    private val allExpenses = mutableListOf<Expense>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        categorySpinner = view.findViewById(R.id.categorySpinner)
        dateSpinner = view.findViewById(R.id.dateSpinner)
        recyclerView = view.findViewById(R.id.recyclerView)
        noItemsTextView = view.findViewById(R.id.noItemsTextView)

        setupRecyclerView()
        loadExpenses()

        return view
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { expense ->
            val i = Intent(requireContext(), ExpenseDetailActivity::class.java).apply {
                putExtra("name", expense.name)
                putExtra("amount", expense.amount)
                putExtra("date", expense.date)
                putExtra("category", expense.category)
                putExtra("description", expense.description)
                putExtra("location", expense.location)
                putExtra("startTime", expense.startTime)
                putExtra("endTime", expense.endTime)
                putExtra("photoPath", expense.photoPath)
            }
            startActivity(i)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadExpenses() {
        FirebaseAuthManager.getCurrentUserId()?.let { uid ->
            lifecycleScope.launch {
                FirestoreManager.getExpenses(uid) { list ->
                    allExpenses.clear()
                    allExpenses.addAll(list)
                    setupSpinners()
                    filterItems()
                }
            }
        }
    }

    private fun setupSpinners() {
        val categoryOptions = listOf("All") + allExpenses.map { it.category }.distinct()
        val dateOptions = listOf("All", "Last 7 Days", "Last 30 Days", "This Month")

        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        dateSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dateOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterItems()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        categorySpinner.onItemSelectedListener = listener
        dateSpinner.onItemSelectedListener = listener
    }

    private fun filterItems() {
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: "All"
        val selectedDate = dateSpinner.selectedItem?.toString() ?: "All"

        val now = Calendar.getInstance()
        val filtered = allExpenses.filter { expense ->
            val categoryMatch = selectedCategory == "All" || expense.category == selectedCategory

            val expenseDate = try {
                dateFormat.parse(expense.date)
            } catch (e: Exception) {
                null
            }

            val dateMatch = when {
                selectedDate == "All" -> true
                expenseDate == null -> false
                selectedDate == "Last 7 Days" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -7)
                    expenseDate.after(cal.time)
                }
                selectedDate == "Last 30 Days" -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    expenseDate.after(cal.time)
                }
                selectedDate == "This Month" -> {
                    val cal = Calendar.getInstance()
                    val expCal = Calendar.getInstance().apply { time = expenseDate }
                    cal.get(Calendar.MONTH) == expCal.get(Calendar.MONTH) &&
                            cal.get(Calendar.YEAR) == expCal.get(Calendar.YEAR)
                }
                else -> true
            }

            categoryMatch && dateMatch
        }

        adapter.submitList(filtered)
        noItemsTextView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
