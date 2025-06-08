package com.example.budgetbee_prog7313_poe_final.ui.income

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Category
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userUid: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        FirebaseApp.initializeApp(this)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userUid = currentUser.uid

        findViewById<Button>(R.id.buttonAddIncome).setOnClickListener {
            saveIncome()
        }
    }

    private fun saveIncome() {
        val name = findViewById<EditText>(R.id.inputIncomeName).text.toString()
        val amount = findViewById<EditText>(R.id.inputIncomeAmount).text.toString().toDoubleOrNull() ?: 0.0

        if (userUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val incomeData = hashMapOf<String, Any>(
            "userUid" to userUid!!,
            "name" to name,
            "amount" to amount
        )

        saveToFirestore(incomeData)
    }

    private fun saveToFirestore(incomeData: HashMap<String, Any>) {
        firestore.collection("incomes")
            .add(incomeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Income Added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add income", Toast.LENGTH_SHORT).show()
            }
    }

}
