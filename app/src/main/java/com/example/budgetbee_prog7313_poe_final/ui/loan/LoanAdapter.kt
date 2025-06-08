package com.example.budgetbee_prog7313_poe_final.ui.loan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.databinding.ItemLoanBinding
import com.example.budgetbee_prog7313_poe_final.model.Loan

class LoanAdapter(
    private val loans: List<Loan>,
    private val onEdit: (Loan) -> Unit,
    private val onDelete: (Loan) -> Unit
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    inner class LoanViewHolder(val b: ItemLoanBinding)
        : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LoanViewHolder(
            ItemLoanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        holder.b.apply {
            loanAmount.text = "Loan: R${"%.2f".format(loan.amount)}"
            interestRate.text = "Interest: ${"%.2f".format(loan.interestRate)}%"
            monthlyRepayment.text = "Monthly: R${"%.2f".format(loan.monthlyPayment)}"
            remainingBalance.text = "Remaining: R${"%.2f".format(loan.remainingAmount)}"

            editLoan.setOnClickListener { onEdit(loan) }
            deleteLoan.setOnClickListener { onDelete(loan) }
        }
    }

    override fun getItemCount(): Int = loans.size
}
