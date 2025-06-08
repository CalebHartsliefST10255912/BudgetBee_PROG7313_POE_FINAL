package com.example.budgetbee_prog7313_poe_final.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.categoryName)
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryText.text = category.name
        holder.itemView.setOnClickListener { onClick(category) }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }
}
