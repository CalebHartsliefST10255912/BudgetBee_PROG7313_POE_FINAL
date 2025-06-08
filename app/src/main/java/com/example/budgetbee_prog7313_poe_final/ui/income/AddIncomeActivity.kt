// src/main/java/com/example/budgetbee_prog7313_poe_final/ui/income/AddIncomeActivity.kt
package com.example.budgetbee_prog7313_poe_final.ui.income

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Income
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    private var userUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        FirebaseAuthManager.getCurrentUserId()?.let {
            userUid = it
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<Button>(R.id.buttonAddIncome).setOnClickListener {
            saveIncome()
        }
    }

    private fun saveIncome() {
        val name   = findViewById<EditText>(R.id.inputIncomeName).text.toString().trim()
        val amount = findViewById<EditText>(R.id.inputIncomeAmount)
            .text.toString().toDoubleOrNull() ?: 0.0
        val date   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val income = Income(
            name    = name,
            amount  = amount,
            date    = date
        )

        FirestoreManager.addIncome(userUid, income) { success ->
            if (success) {
                Toast.makeText(this, "Income Added", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
