package com.example.budgetbee_prog7313_poe_final.ui.category

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirebaseAuthManager
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Category

class CategoryFragment : Fragment() {

    private lateinit var adapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private var userUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userUid = FirebaseAuthManager.getCurrentUserId()
        if (userUid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        // 1) bind recycler
        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // 2) bind and wire the button
        view.findViewById<Button>(R.id.buttonAddCategory)
            .setOnClickListener { showAddCategoryDialog() }

        // 3) load & display categories
        loadCategories()
    }


    private fun loadCategories() {
        FirestoreManager.getCategories(userUid!!) { categories ->
            if (categories.isEmpty()) {
                FirestoreManager.initializeDefaultCategories(userUid!!) { success ->
                    if (success) loadCategories()
                    else Toast.makeText(requireContext(), "Failed to load default categories", Toast.LENGTH_SHORT).show()
                }
            } else {
                val (addCategory, otherCategories) = categories.partition { it.name == "Add" }
                val orderedCategories = otherCategories + addCategory

                adapter = CategoryAdapter(orderedCategories) { selectedCategory ->
                    if (selectedCategory.name == "Add") {
                        showAddCategoryDialog()
                    } else {
                        openCategoryExpensesActivity(selectedCategory.name)
                    }
                }

                recyclerView.adapter = adapter
            }
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add New Category")

        val input = EditText(requireContext())
        input.hint = "Enter category name"
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                val category = Category(
                    name = name,
                    iconResId = R.drawable.blue_circle,
                    isDefault = false
                )

                FirestoreManager.addCustomCategory(userUid!!, category) { success ->
                    if (success) loadCategories()
                    else Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun openCategoryExpensesActivity(categoryName: String) {
        val intent = Intent(requireContext(), CategoryDetailsActivity::class.java)
        intent.putExtra("CATEGORY_NAME", categoryName)
        startActivity(intent)
    }
}
