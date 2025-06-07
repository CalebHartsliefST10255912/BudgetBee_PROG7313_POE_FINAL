package com.example.budgetbee_prog7313_poe_final.ui.category

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.model.Category
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class CategoryFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: CategoryAdapter
    private var userId: Int = -1
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FirebaseApp.initializeApp(requireContext())
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = requireContext().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
            .getInt("userId", -1)

        if (userId == -1) {
            startActivity(Intent(requireContext(), FirebaseAuthManager::class.java))
            requireActivity().finish()
            return
        }

        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categoriesSnapshot = firestore.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val categories = categoriesSnapshot.toObjects(Category::class.java).toMutableList()

            if (categories.isEmpty()) {
                val predefined = listOf(
                    Category(userId = userId.toString(), name = "Food", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Transport", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Medicine", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Groceries", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Rent", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Gifts", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Savings", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Entertainment", iconResId = R.drawable.blue_circle),
                    Category(userId = userId.toString(), name = "Add", iconResId = R.drawable.blue_circle)
                )

                predefined.forEach {
                    firestore.collection("categories").add(it).await()
                }

                categories.addAll(predefined)
            }

            val (addCategory, otherCategories) = categories.partition { it.name == "Add" }
            val orderedCategories = otherCategories + addCategory

            adapter = CategoryAdapter(orderedCategories) { selectedCategory ->
                if (selectedCategory.name == "Add") {
                    showAddCategoryDialog()
                } else {
                    val intent = Intent(requireContext(), CategoryDetailsActivity::class.java)
                    intent.putExtra("CATEGORY_NAME", selectedCategory.name)
                    intent.putExtra("CATEGORY_ID", selectedCategory.categoryId) // Optional
                    startActivity(intent)
                }
            }

            recyclerView.adapter = adapter
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Category")

        val input = EditText(requireContext())
        input.hint = "Enter category name"
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                val newCategory = Category(
                    userId = userId.toString(),
                    name = categoryName,
                    iconResId = R.drawable.blue_circle
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    firestore.collection("categories").add(newCategory).await()
                    refreshCategoryList()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun refreshCategoryList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val snapshot = firestore.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val updatedCategories = snapshot.toObjects(Category::class.java)

            val (addCategory, otherCategories) = updatedCategories.partition { it.name == "Add" }
            val orderedCategories = otherCategories + addCategory

            adapter.updateCategories(orderedCategories)
        }
    }
}
