package com.example.budgetbee_prog7313_poe_final.ui.search

import android.app.DatePickerDialog
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
import com.example.budgetbee_prog7313_poe_final.ui.search.GraphActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {

    private lateinit var categorySpinner: Spinner
    private lateinit var fromDateBtn: Button
    private lateinit var toDateBtn: Button
    private lateinit var filterBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var noItemsTextView: TextView
    private lateinit var adapter: ExpenseAdapter

    private val allExpenses = mutableListOf<Expense>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var fromDate: Date? = null
    private var toDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        categorySpinner = view.findViewById(R.id.categoryFilterSpinner)
        fromDateBtn = view.findViewById(R.id.fromDateButton)
        toDateBtn = view.findViewById(R.id.toDateButton)
        filterBtn = view.findViewById(R.id.applyFilterButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        noItemsTextView = view.findViewById(R.id.noItemsTextView)

        setupRecyclerView()
        loadExpenses()

        fromDateBtn.setOnClickListener {
            showDatePicker { date ->
                fromDate = date
                fromDateBtn.text = "From: ${dateFormat.format(date)}"
            }
        }

        toDateBtn.setOnClickListener {
            showDatePicker { date ->
                toDate = date
                toDateBtn.text = "To: ${dateFormat.format(date)}"
            }
        }

        filterBtn.setOnClickListener {
            filterItems()
        }

        // Corrected button ID from viewGrapButton to viewGraphButton
        view.findViewById<Button>(R.id.viewGraphButton).setOnClickListener {
            startActivity(Intent(requireContext(), GraphActivity::class.java))
        }

        return view
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter { expense ->
            val intent = Intent(requireContext(), ExpenseDetailActivity::class.java).apply {
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
            startActivity(intent)
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

        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryOptions
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterItems()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun filterItems() {
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: "All"

        val filtered = allExpenses.filter { expense ->
            val categoryMatch = selectedCategory == "All" || expense.category == selectedCategory

            val expenseDate = try {
                dateFormat.parse(expense.date)
            } catch (e: Exception) {
                null
            }

            val dateMatch = when {
                fromDate != null && toDate != null && expenseDate != null ->
                    !expenseDate.before(fromDate) && !expenseDate.after(toDate)
                fromDate != null && expenseDate != null ->
                    !expenseDate.before(fromDate)
                toDate != null && expenseDate != null ->
                    !expenseDate.after(toDate)
                else -> true
            }

            categoryMatch && dateMatch
        }

        adapter.submitList(filtered)
        noItemsTextView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
