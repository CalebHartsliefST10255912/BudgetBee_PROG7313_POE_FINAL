package com.example.budgetbee_prog7313_poe_final.ui.goal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee_prog7313_poe_final.databinding.ActivityGoalsBinding
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Goal
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding

    // Must match FirestoreManager.saveGoal's month format
    private val monthKey: String by lazy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Get current user ID
        val userId = FirebaseAuthManager.getCurrentUserId() ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2) Load existing goals
        FirestoreManager.getGoal(userId) { goals: List<Goal> ->
            // Find the goal for this month, if any
            goals.find { it.month == monthKey }?.let { goal ->
                binding.editMinGoal.setText(goal.minGoal.toString())
                binding.editMaxGoal.setText(goal.maxGoal.toString())
            }
        }

        // 3) Wire Save button
        binding.btnSaveGoal.setOnClickListener {
            val min = binding.editMinGoal.text.toString().toDoubleOrNull()
            val max = binding.editMaxGoal.text.toString().toDoubleOrNull()

            if (min == null || max == null) {
                Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirestoreManager.saveGoal(userId, min, max) { success ->
                if (success) {
                    Toast.makeText(this, "Goals saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
