package com.example.budgetbee_prog7313_poe_final.ui.expense

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private var categoryId = -1
    private lateinit var categoryName: String
    private lateinit var categoriesList: List<Category>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        FirebaseApp.initializeApp(this)

        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Load categories from Firestore
        firestore.collection("categories")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                categoriesList = result.documents.map {
                    Category(
                        categoryId = it.getLong("categoryId")?.toInt() ?: 0,
                        name = it.getString("name") ?: ""
                    )
                }

                val categoryNames = categoriesList.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                findViewById<Spinner>(R.id.categorySpinner).adapter = adapter

                findViewById<Spinner>(R.id.categorySpinner).onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            categoryId = categoriesList[position].categoryId
                            categoryName = categoriesList[position].name
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
            }

        findViewById<Button>(R.id.buttonPickImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        findViewById<Button>(R.id.buttonAddExpense).setOnClickListener {
            saveExpense()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        }
    }

    private fun saveExpense() {
        val name = findViewById<EditText>(R.id.inputExpenseName).text.toString()
        val amount = findViewById<EditText>(R.id.inputExpenseAmount).text.toString().toDoubleOrNull() ?: 0.0
        val description = findViewById<EditText>(R.id.inputExpenseDescription).text.toString()
        val location = findViewById<EditText>(R.id.inputExpenseLocation).text.toString()
        val startTime = findViewById<EditText>(R.id.inputStartTime).text.toString()
        val endTime = findViewById<EditText>(R.id.inputEndTime).text.toString()

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseData = hashMapOf(
            "userId" to userId,
            "categoryId" to categoryId,
            "name" to name,
            "amount" to amount,
            "date" to date,
            "startTime" to startTime,
            "endTime" to endTime,
            "category" to categoryName,
            "description" to description,
            "location" to location
        )

        if (imageUri != null) {
            val fileRef = storage.reference.child("expenses/${UUID.randomUUID()}")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        expenseData["photoPath"] = uri.toString()
                        firestore.collection("expenses")
                            .add(expenseData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            firestore.collection("expenses")
                .add(expenseData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}
