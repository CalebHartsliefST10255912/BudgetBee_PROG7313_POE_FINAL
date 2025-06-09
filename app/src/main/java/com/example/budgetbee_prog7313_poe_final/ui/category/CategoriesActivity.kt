package com.example.budgetbee_prog7313_poe_final.ui.category

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.LoginActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Category

class CategoriesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var userUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hide the Action Bar
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_category)

        recyclerView = findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    override fun onStart() {
        super.onStart()

        val uid = FirebaseAuthManager.getCurrentUserId()
        if (uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userUid = uid

        FirestoreManager.getUser(uid) { user ->
            if (user == null) {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                FirebaseAuthManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                loadCategories()
            }
        }
    }

    private fun loadCategories() {
        FirestoreManager.getCategories(userUid!!) { categories ->
            if (categories.isEmpty()) {
                FirestoreManager.initializeDefaultCategories(userUid!!) { success ->
                    if (success) {
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to initialize categories", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val (moreCategory, otherCategories) = categories.partition { it.name == "Add" }
                val orderedCategories = otherCategories + moreCategory

                adapter = CategoryAdapter(orderedCategories) { selectedCategory ->
                    if (selectedCategory.name == "Add") {
                        showAddCategoryDialog()
                    } else {
                        val intent = Intent(this, CategoryDetailsActivity::class.java)
                        intent.putExtra("CATEGORY_ID", selectedCategory.categoryId)
                        intent.putExtra("CATEGORY_NAME", selectedCategory.name)
                        startActivity(intent)
                    }
                }

                recyclerView.adapter = adapter
            }
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Category")

        val input = EditText(this)
        input.hint = "Enter category name"
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                val newCategory = Category(
                    name = categoryName,
                    iconResId = R.drawable.blue_circle,
                    isDefault = false
                )

                FirestoreManager.addCustomCategory(userUid!!, newCategory) { success ->
                    if (success) {
                        loadCategories()
                    } else {
                        Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
