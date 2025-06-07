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
import com.example.budgetbee_prog7313_poe_final.LoginActivity
import com.example.budgetbee_prog7313_poe_final.databinding.FragmentHomeBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
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
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        // Dummy values for progress demonstration
        val dummyMin = 100.0
        val dummyMax = 200.0
        val dummyCurrent = 150.0
        setupProgressBar(dummyMin, dummyMax, dummyCurrent)

        // Save goal logic
        binding.btnSaveGoal.setOnClickListener {
            val minGoal = binding.editMinGoal.text.toString().toDoubleOrNull()
            val maxGoal = binding.editMaxGoal.text.toString().toDoubleOrNull()
            if (minGoal == null || maxGoal == null) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveGoal(minGoal, maxGoal) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Goals saved!", Toast.LENGTH_SHORT).show()
                    // Reuse dummyCurrent or calculate real current later
                    setupProgressBar(minGoal, maxGoal, dummyCurrent)
                } else {
                    Toast.makeText(requireContext(), "Failed to save goals", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return root
    }

    private fun setupProgressBar(minGoal: Double, maxGoal: Double, current: Double) {
        val percent = when {
            current <= minGoal -> 0
            current >= maxGoal -> 100
            else -> (((current - minGoal) / (maxGoal - minGoal)) * 100).toInt()
        }
        binding.goalProgressBar.max = 100
        binding.goalProgressBar.progress = percent
        binding.progressText.text = "$percent% of range"
    }

    private fun saveGoal(minGoal: Double, maxGoal: Double, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return callback(false)
        }
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val goalData = hashMapOf(
            "minGoal" to minGoal,
            "maxGoal" to maxGoal,
            "month" to currentMonth
        )
        FirebaseFirestore.getInstance()
            .collection("goals")
            .document("$userId-$currentMonth")
            .set(goalData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error saving goals", e)
                callback(false)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
