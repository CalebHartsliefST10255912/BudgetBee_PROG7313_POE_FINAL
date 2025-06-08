// ExpenseFragment.kt
package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.ui.goal.GoalsActivity
import com.example.budgetbee_prog7313_poe_final.ui.income.AddIncomeActivity
import com.example.budgetbee_prog7313_poe_final.ui.income.AllIncomeActivity

class ExpenseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_expense, container, false)
        view.findViewById<Button>(R.id.buttonAddExpense).setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
        view.findViewById<Button>(R.id.buttonViewTransactions).setOnClickListener {
            startActivity(Intent(requireContext(), TransactionActivity::class.java))
        }

        // When the button is clicked, navigate to the AddIncomeActivity
        view.findViewById<Button>(R.id.buttonAddIncome).setOnClickListener {
            startActivity(Intent(requireContext(), AddIncomeActivity::class.java))
        }
        view.findViewById<Button>(R.id.buttonViewIncome).setOnClickListener {
            startActivity(Intent(requireContext(), AllIncomeActivity::class.java))
        }
        return view
    }
}
