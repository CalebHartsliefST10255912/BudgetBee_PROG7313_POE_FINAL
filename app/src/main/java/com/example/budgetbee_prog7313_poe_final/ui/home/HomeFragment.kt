package com.example.budgetbee_prog7313_poe_final.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.budgetbee_prog7313_poe_final.LoginActivity
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentHomeBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.ui.GoalsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        // Greeting
        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }
        homeViewModel.loadUserGreeting()

        // Logout
        binding.logoutButton.setOnClickListener {
            FirebaseAuthManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Edit Goals button
        binding.btnEditGoals.setOnClickListener {
            startActivity(Intent(requireContext(), GoalsActivity::class.java))
        }

        // Load & display saved goals
        loadAndDisplayGoals()

        return root
    }

    private fun loadAndDisplayGoals() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        FirebaseFirestore.getInstance()
            .collection("goals")
            .document("$userId-$month")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val minGoal = doc.getDouble("minGoal") ?: 0.0
                    val maxGoal = doc.getDouble("maxGoal") ?: 0.0
                    binding.minGoalText.text = "Min: R$minGoal"
                    binding.maxGoalText.text = "Max: R$maxGoal"
                    val dummyCurrent = (minGoal + maxGoal) / 2
                    setupProgressBar(minGoal, maxGoal, dummyCurrent)
                } else {
                    binding.minGoalText.text = "Min: –"
                    binding.maxGoalText.text = "Max: –"
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error loading goals", e)
            }
    }

    private fun setupProgressBar(min: Double, max: Double, current: Double) {
        val percent = when {
            current <= min -> 0
            current >= max -> 100
            else -> (((current - min) / (max - min)) * 100).toInt()
        }
        binding.goalProgressBar.max = 100
        binding.goalProgressBar.progress = percent
        binding.progressText.text = "$percent% of range"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
