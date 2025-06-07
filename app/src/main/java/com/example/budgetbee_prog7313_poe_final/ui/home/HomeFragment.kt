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

        // Observe greeting text
        homeViewModel.text.observe(viewLifecycleOwner) { text ->
            binding.textHome.text = text
        }
        homeViewModel.loadUserGreeting()

        // Logout button
        binding.logoutButton.setOnClickListener {
            FirebaseAuthManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // Goal inputs and save button
        val minGoalInput = binding.editMinGoal
        val maxGoalInput = binding.editMaxGoal
        val saveGoalBtn = binding.btnSaveGoal

        saveGoalBtn.setOnClickListener {
            val minGoal = minGoalInput.text.toString().toDoubleOrNull()
            val maxGoal = maxGoalInput.text.toString().toDoubleOrNull()
            if (minGoal == null || maxGoal == null) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveGoal(minGoal, maxGoal) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Goals saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to save goals", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to save goals to Firestore
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
}
