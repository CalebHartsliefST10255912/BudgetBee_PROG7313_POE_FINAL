// ExpenseAdapter.kt
package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Expense

class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses: List<Expense> = emptyList()

    inner class ExpenseViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView   = v.findViewById(R.id.textExpenseName)
        val amount: TextView = v.findViewById(R.id.textExpenseAmount)
        val date: TextView   = v.findViewById(R.id.textExpenseDate)
        // no image here unless you add it to item_expense.xml
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ExpenseViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_expense, parent, false)
        )

    override fun onBindViewHolder(holder: ExpenseViewHolder, pos: Int) {
        val e = expenses[pos]
        holder.name.text   = e.name
        holder.amount.text = "R %.2f".format(e.amount)
        holder.date.text   = e.date

        holder.itemView.setOnClickListener { onClick(e) }
    }

    override fun getItemCount() = expenses.size

    fun submitList(list: List<Expense>) {
        expenses = list
        notifyDataSetChanged()
    }
}
