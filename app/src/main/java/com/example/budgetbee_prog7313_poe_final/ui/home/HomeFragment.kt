package com.example.budgetbee_prog7313_poe_final.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.budgetbee_prog7313_poe_final.LoginActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentHomeBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Goal
import com.example.budgetbee_prog7313_poe_final.model.Reward
import com.example.budgetbee_prog7313_poe_final.ui.goal.GoalsActivity
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth

import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Must match FirestoreManager.saveGoal's month format
    private val monthKey by lazy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        //Button to go to the missions board
        binding.btnGoToMissions.setOnClickListener {
            findNavController().navigate(R.id.missionsFragment)
        }

        //Button to go to the Rewards page
        binding.btnGoToRewards.setOnClickListener {
            findNavController().navigate(R.id.RewardFragment)
        }

        //Button to go to the user's rewards page
        binding.btnGoToMyRewards.setOnClickListener {
            findNavController().navigate(R.id.myRewardFragment)
        }

        // Greeting
        homeViewModel.text.observe(viewLifecycleOwner) { binding.textHome.text = it }
        homeViewModel.loadUserGreeting()

        // Logout
        binding.logoutButton.setOnClickListener {
            FirebaseAuthManager.logout()
            Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(this)
            }
        }

        // Edit Goals
        binding.btnEditGoals.setOnClickListener {
            startActivity(Intent(requireContext(), GoalsActivity::class.java))
        }

        // Load, then chain-fetch incomes & expenses
        loadAndDisplayGoals()

        return binding.root
    }

    private fun loadAndDisplayGoals() {
        val userId = FirebaseAuthManager.getCurrentUserId() ?: return

        FirestoreManager.getGoal(userId) { goals ->
            val thisMonthGoal = goals.find { it.month == monthKey }
            if (thisMonthGoal != null) {
                val min = thisMonthGoal.minGoal
                val max = thisMonthGoal.maxGoal
                binding.minGoalText.text = "Min: R$min"
                binding.maxGoalText.text = "Max: R$max"

                // Fetch only expenses for the month
                FirestoreManager.getExpenses(userId) { expenses ->
                    // Sum up all expenses
                    val totalExpenses = expenses.sumOf { it.amount }
                    Log.d("HomeFragment", "Expenses total: $totalExpenses, min=$min, max=$max")

                    // Compute percent of range
                    val percent = when {
                        totalExpenses <= min -> 0
                        totalExpenses >= max -> 100
                        else -> (((totalExpenses - min) / (max - min)) * 100).toInt()
                    }

                    // Update UI
                    binding.goalProgressBar.max = 100
                    binding.goalProgressBar.progress = percent
                    binding.progressText.text = "$percent% of spending range"
                }
            } else {
                // No goal set yet
                binding.minGoalText.text = "Min: –"
                binding.maxGoalText.text = "Max: –"
                binding.goalProgressBar.apply {
                    max = 100; progress = 0
                }
                binding.progressText.text = ""
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayGoals()
    }
}
