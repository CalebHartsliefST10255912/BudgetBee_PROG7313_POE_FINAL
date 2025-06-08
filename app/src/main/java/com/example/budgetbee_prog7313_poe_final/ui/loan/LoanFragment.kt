package com.example.budgetbee_prog7313_poe_final.ui.loan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentLoanBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Loan
import com.google.firebase.auth.FirebaseAuth

class LoanFragment : Fragment() {
    private var _binding: FragmentLoanBinding? = null
    private val binding get() = _binding!!

    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanBinding.inflate(inflater, container, false)
        val root = binding.root

        recyclerView = binding.loanRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return root

        binding.btnAddLoan.setOnClickListener {
            showLoanDialog(null)
        }

        loadLoans()
        return root
    }

    private fun loadLoans() {
        FirestoreManager.getLoans(uid) { list ->
            recyclerView.adapter = LoanAdapter(
                loans = list,
                onEdit = { loan -> showLoanDialog(loan) },
                onDelete = { loan ->
                    FirestoreManager.deleteLoan(uid, loan.id) { loadLoans() }
                }
            )
        }
    }

    private fun showLoanDialog(existing: Loan?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_loan, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etRate = dialogView.findViewById<EditText>(R.id.etRate)
        val etMin = dialogView.findViewById<EditText>(R.id.etMinRepay)
        val etPaid = dialogView.findViewById<EditText>(R.id.etPaid)
        val etMonths = dialogView.findViewById<EditText>(R.id.etMonths)

        // prefill
        existing?.let {
            etAmount.setText(it.amount.toString())
            etRate.setText(it.interestRate.toString())
            etMin.setText(it.minRepayment.toString())
            etPaid.setText(it.amountPaid.toString())
            etMonths.setText(it.targetRepaymentMonths.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Add Loan" else "Edit Loan")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val loan = Loan(
                    id = existing?.id ?: "",
                    amount = etAmount.text.toString().toDouble(),
                    interestRate = etRate.text.toString().toDouble(),
                    minRepayment = etMin.text.toString().toDouble(),
                    amountPaid = etPaid.text.toString().toDouble(),
                    targetRepaymentMonths = etMonths.text.toString().toInt()
                )

                if (existing == null) {
                    FirestoreManager.addLoan(uid, loan) { loadLoans() }
                } else {
                    FirestoreManager.updateLoan(uid, loan) { loadLoans() }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
