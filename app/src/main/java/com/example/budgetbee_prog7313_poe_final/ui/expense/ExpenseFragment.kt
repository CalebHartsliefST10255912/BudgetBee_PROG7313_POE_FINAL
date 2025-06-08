package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.ui.income.AddIncomeActivity

class ExpenseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expense, container, false)

        val buttonAddExpense = view.findViewById<Button>(R.id.buttonAddExpense)
        val buttonAddIncome = view.findViewById<Button>(R.id.buttonAddIncome)
        val buttonViewTransactions = view.findViewById<Button>(R.id.buttonViewTransactions)

        buttonAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }

        buttonAddIncome.setOnClickListener {
            startActivity(Intent(requireContext(), AddIncomeActivity::class.java))
        }

        buttonViewTransactions.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionActivity::class.java))
        }

        return view
    }
}
