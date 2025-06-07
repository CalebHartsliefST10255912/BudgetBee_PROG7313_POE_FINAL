package com.example.budgetbee_prog7313_poe_final.ui.category

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.LoginActivity
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoriesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userUid: String? = null


    private val categoryCollectionName = "categories"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_category)

        // Initialize FirebaseAuth
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreManager.getUser(uid) { user ->
            if (user != null) {
                firestore = FirebaseFirestore.getInstance()
                recyclerView = findViewById(R.id.categoryRecyclerView)
                recyclerView.layoutManager = GridLayoutManager(this, 3)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        loadCategories()
    }

    private fun loadCategories() {
        val userCategoriesRef = firestore.collection("users")
            .document(userUid!!)
            .collection(categoryCollectionName)

        userCategoriesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val predefined = listOf(
                    Category(name = "Food", iconResId = R.drawable.blue_circle),
                    Category(name = "Transport", iconResId = R.drawable.blue_circle),
                    Category(name = "Medicine", iconResId = R.drawable.blue_circle),
                    Category(name = "Groceries", iconResId = R.drawable.blue_circle),
                    Category(name = "Rent", iconResId = R.drawable.blue_circle),
                    Category(name = "Gifts", iconResId = R.drawable.blue_circle),
                    Category(name = "Savings", iconResId = R.drawable.blue_circle),
                    Category(name = "Entertainment", iconResId = R.drawable.blue_circle),
                    Category(name = "Add", iconResId = R.drawable.blue_circle)
                )

                predefined.forEach {
                    userCategoriesRef.add(it)
                }

                // Re-fetch after adding
                loadCategories()
            } else {
                val categories = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val iconResId = (doc.getLong("iconResId") ?: R.drawable.blue_circle).toString()
                    Category(doc.id, name, iconResId)
                }

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
                val newCategory = Category(name = categoryName, iconResId = R.drawable.blue_circle)

                firestore.collection("users")
                    .document(userUid!!)
                    .collection(categoryCollectionName)
                    .add(newCategory)
                    .addOnSuccessListener {
                        loadCategories()
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
