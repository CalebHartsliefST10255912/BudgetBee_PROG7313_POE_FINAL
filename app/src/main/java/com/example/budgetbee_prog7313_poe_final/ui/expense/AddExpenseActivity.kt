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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private var categoryId: String = ""
    private lateinit var categoryName: String
    private lateinit var categoriesList: List<Category>
    private var userUid: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        FirebaseApp.initializeApp(this)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userUid = currentUser.uid

        // Load categories from user's Firestore collection
        firestore.collection("users")
            .document(userUid!!)
            .collection("categories")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Add default categories
                    val defaultCategories = listOf("Food", "Transport", "Shopping", "Health", "Entertainment")

                    val batch = firestore.batch()
                    val userCategoriesRef = firestore.collection("users").document(userUid!!).collection("categories")

                    defaultCategories.forEach { name ->
                        val docRef = userCategoriesRef.document() // auto-ID
                        val data = hashMapOf("name" to name)
                        batch.set(docRef, data)
                    }

                    batch.commit().addOnSuccessListener {
                        loadCategories() // reload after adding
                    }
                } else {
                    categoriesList = result.documents.map {
                        Category(
                            categoryId = it.id,
                            name = it.getString("name") ?: ""
                        )
                    }

                    //setupSpinner()
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

        if (userUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseData = hashMapOf<String, Any>(
            "userUid" to userUid!!,
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
                        uri?.toString()?.let {
                            expenseData["photoPath"] = it
                        }

                        saveToFirestore(expenseData)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveToFirestore(expenseData)
        }
    }

    private fun saveToFirestore(expenseData: HashMap<String, Any>) {
        firestore.collection("expenses")
            .add(expenseData)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCategories() {
        firestore.collection("users")
            .document(userUid!!)
            .collection("categories")
            .get()
            .addOnSuccessListener { result ->
                categoriesList = result.documents.map {
                    Category(
                        categoryId = it.id,
                        name = it.getString("name") ?: ""
                    )
                }
                setupSpinner()
            }
    }

    private fun setupSpinner() {
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

}
