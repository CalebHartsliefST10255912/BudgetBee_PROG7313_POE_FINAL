package com.example.budgetbee_prog7313_poe_final.ui.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    private lateinit var categorySpinner: Spinner
    private lateinit var fromDateBtn: Button
    private lateinit var toDateBtn: Button
    private lateinit var filterBtn: Button

    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var fromDate: Date? = null
    private var toDate: Date? = null
    private var allExpenses: List<Expense> = emptyList()
    private var categories: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        chart = findViewById(R.id.categoryLineChart)
        categorySpinner = findViewById(R.id.categoryFilterSpinner)
        fromDateBtn = findViewById(R.id.fromDateButton)
        toDateBtn = findViewById(R.id.toDateButton)
        filterBtn = findViewById(R.id.applyFilterButton)

        loadExpenses()

        fromDateBtn.setOnClickListener { pickDate { fromDate = it; updateButtonText() } }
        toDateBtn.setOnClickListener { pickDate { toDate = it; updateButtonText() } }
        filterBtn.setOnClickListener { applyFilter() }
    }

    private fun pickDate(callback: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            callback(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateButtonText() {
        fromDateBtn.text = "From: ${fromDate?.let { dateFormat.format(it) } ?: "Select"}"
        toDateBtn.text = "To: ${toDate?.let { dateFormat.format(it) } ?: "Select"}"
    }

    private fun loadExpenses() {
        val userId = auth.currentUser?.uid ?: return
        FirestoreManager.getExpenses(userId) { list ->
            allExpenses = list
            categories = list.map { it.category }.distinct().sorted()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("All") + categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
    }

    private fun applyFilter() {
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: "All"

        val filtered = allExpenses.filter { expense ->
            val date = try { dateFormat.parse(expense.date) } catch (e: Exception) { null }
            if (date == null) return@filter false

            val inRange = (fromDate == null || !date.before(fromDate)) &&
                    (toDate == null || !date.after(toDate))
            val categoryMatch = selectedCategory == "All" || expense.category == selectedCategory

            inRange && categoryMatch
        }.sortedBy { it.date }

        updateChart(filtered)
    }

    private fun updateChart(expenses: List<Expense>) {
        val grouped = expenses.groupBy { it.date }
        val sortedDates = grouped.keys.sorted()
        val entries = sortedDates.mapIndexed { index, dateStr ->
            val total = grouped[dateStr]?.sumOf { it.amount } ?: 0.0
            Entry(index.toFloat(), total.toFloat())
        }

        val labels = sortedDates
        val dataSet = LineDataSet(entries, "Spending Over Time").apply {
            circleRadius = 4f
            setDrawFilled(true)
            lineWidth = 2f
        }

        chart.data = LineData(dataSet)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.axisLeft.removeAllLimitLines()

        // Add goal lines
        val userId = auth.currentUser?.uid ?: return
        FirestoreManager.getGoal(userId) { goals ->
            val thisMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val goal = goals.find { it.month == thisMonth }
            goal?.let {
                chart.axisLeft.addLimitLine(LimitLine(it.minGoal.toFloat(), "Min Goal"))
                chart.axisLeft.addLimitLine(LimitLine(it.maxGoal.toFloat(), "Max Goal"))
            }

            chart.invalidate()
        }
    }
}
