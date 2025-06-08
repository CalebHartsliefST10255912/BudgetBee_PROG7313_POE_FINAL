package com.example.budgetbee_prog7313_poe_final.ui.loan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentLoanBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Loan
import com.google.firebase.auth.FirebaseAuth

class LoanFragment : Fragment() {
    private var _b: FragmentLoanBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentLoanBinding.inflate(inflater, container, false)
        val rv = b.loanRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return b.root

        // Helper to reload data:
        fun load() {
            FirestoreManager.getLoans(uid) { list ->
                rv.adapter = LoanAdapter(
                    list,
                    onEdit = { loan -> showDialog(loan, uid) },
                    onDelete = { loan ->
                        FirestoreManager.deleteLoan(uid, loan.id) { load() }
                    }
                )
            }
        }

        load()  // initial data load

        b.btnAddLoan.setOnClickListener { showDialog(null, uid) }
        return b.root
    }

    private fun showDialog(existing: Loan?, uid: String) {
        val view = layoutInflater.inflate(R.layout.dialog_add_loan, null)
        val eAmount = view.findViewById<EditText>(R.id.etAmount)
        val eRate   = view.findViewById<EditText>(R.id.etRate)
        val eMin    = view.findViewById<EditText>(R.id.etMinRepay)
        val ePaid   = view.findViewById<EditText>(R.id.etPaid)
        val eMonths = view.findViewById<EditText>(R.id.etMonths)

        existing?.let {
            eAmount.setText(it.amount.toString())
            eRate.setText(it.interestRate.toString())
            eMin.setText(it.minRepayment.toString())
            ePaid.setText(it.amountPaid.toString())
            eMonths.setText(it.targetRepaymentMonths.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Add Loan" else "Edit Loan")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val loan = Loan(
                    id = existing?.id ?: "",
                    amount = eAmount.text.toString().toDouble(),
                    interestRate = eRate.text.toString().toDouble(),
                    minRepayment = eMin.text.toString().toDouble(),
                    amountPaid   = ePaid.text.toString().toDouble(),
                    targetRepaymentMonths = eMonths.text.toString().toInt()
                )
                FirestoreManager.addLoan(uid, loan) {
                    // refresh after save
                    view?.post { (b.loanRecyclerView.adapter as? LoanAdapter)?.let { load() } }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun load() {
        // this is overridden by the local load() in onCreateView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
