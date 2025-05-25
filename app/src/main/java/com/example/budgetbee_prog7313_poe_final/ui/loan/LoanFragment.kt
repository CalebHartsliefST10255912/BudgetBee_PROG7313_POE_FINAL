package com.example.budgetbee_prog7313_poe_final.ui.loan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentLoanBinding

class LoanFragment : Fragment() {

    private var _binding: FragmentLoanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val loanViewModel =
            ViewModelProvider(this).get(LoanViewModel::class.java)

        _binding = FragmentLoanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textLoan
        loanViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}