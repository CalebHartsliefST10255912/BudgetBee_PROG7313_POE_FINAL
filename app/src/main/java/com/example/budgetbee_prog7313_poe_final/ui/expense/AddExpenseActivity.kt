package com.example.budgetbee_prog7313_poe_final.ui.expense

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Category
import com.example.budgetbee_prog7313_poe_final.model.Expense
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var categoriesList: List<Category>
    private var userUid: String = ""
    private var categoryId: String = ""
    private var categoryName: String = ""

    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        FirebaseAuthManager.getCurrentUserId()?.let {
            userUid = it
        } ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI
        val dateText = findViewById<TextView>(R.id.textSelectedDate)
        dateText.text = "Selected Date: $selectedDate"

        findViewById<Button>(R.id.buttonSelectDate).setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                    dateText.text = "Selected Date: $selectedDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        findViewById<Button>(R.id.buttonPickImage).setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK).apply { type = "image/*" },
                PICK_IMAGE_REQUEST
            )
        }

        findViewById<Button>(R.id.buttonAddExpense).setOnClickListener {
            saveExpense()
        }

        loadCategories()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            findViewById<ImageView>(R.id.imagePreview).setImageURI(imageUri)
        }
    }

    private fun loadCategories() {
        FirestoreManager.getCategories(userUid) { cats ->
            if (cats.isEmpty()) {
                FirestoreManager.initializeDefaultCategories(userUid) { success ->
                    if (success) loadCategories()
                    else Toast.makeText(this, "Failed to initialize categories", Toast.LENGTH_SHORT).show()
                }
            } else {
                categoriesList = cats
                setupSpinner()
            }
        }
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.categorySpinner)
        val names = categoriesList.map { it.name }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: android.view.View?, pos: Int, id: Long) {
                categoryId = categoriesList[pos].categoryId
                categoryName = categoriesList[pos].name
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }
    }

    private fun saveExpense() {
        val name        = findViewById<EditText>(R.id.inputExpenseName).text.toString()
        val amount      = findViewById<EditText>(R.id.inputExpenseAmount).text.toString().toDoubleOrNull() ?: 0.0
        val description = findViewById<EditText>(R.id.inputExpenseDescription).text.toString()
        val location    = findViewById<EditText>(R.id.inputExpenseLocation).text.toString()
        val startTime   = findViewById<EditText>(R.id.inputStartTime).text.toString()
        val endTime     = findViewById<EditText>(R.id.inputEndTime).text.toString()

        val baseExpense = Expense(
            categoryId  = categoryId,
            category    = categoryName,
            name        = name,
            amount      = amount,
            date        = selectedDate,
            startTime   = startTime,
            endTime     = endTime,
            description = description,
            location    = location,
            photoPath   = null
        )

        fun post(expense: Expense) {
            FirestoreManager.addExpense(userUid, expense) { success ->
                if (success) {
                    Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (imageUri != null) {
            val fileRef = storage.reference.child("expenses/${UUID.randomUUID()}")
            fileRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        post(baseExpense.copy(photoPath = uri.toString()))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            post(baseExpense)
        }
    }
}
