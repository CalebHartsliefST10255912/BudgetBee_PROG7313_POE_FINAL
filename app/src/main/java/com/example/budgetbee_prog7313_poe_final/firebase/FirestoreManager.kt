package com.example.budgetbee_prog7313_poe_final.firebase

import com.example.budgetbee_prog7313_poe_final.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

import com.example.budgetbee_prog7313_poe_final.model.Goal
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
///
    // USERS
    fun addUser(userId: String, user: User, onResult: (Boolean) -> Unit) {
        db.collection("users").document(userId).set(user)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUser(userId: String, onResult: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    // CATEGORIES
    fun addCategory(category: Category, onResult: (Boolean) -> Unit) {
        db.collection("categories").add(category)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getCategories(userId: String, onResult: (List<Category>) -> Unit) {
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject<Category>() }
                onResult(list)
            }
    }

    // EXPENSES
    fun addExpense(expense: Expense, onResult: (Boolean) -> Unit) {
        db.collection("expenses").add(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getExpenses(userId: String, onResult: (List<Expense>) -> Unit) {
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject<Expense>() }
                onResult(list)
            }
    }

    // GOALS
    fun setGoal(userId: String, goal: Goal, onResult: (Boolean) -> Unit) {
        db.collection("goals").document(userId).set(goal)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getGoal(userId: String, onResult: (Goal?) -> Unit) {
        db.collection("goals").document(userId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject<Goal>())
            }
    }




    ///////////////////////////////////////////////////////////



    fun saveGoal(minGoal: Double, maxGoal: Double, onComplete: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val currentMonth = sdf.format(Date())

        val goal = Goal(
            userId = userId,
            minGoal = minGoal,
            maxGoal = maxGoal,
            month = currentMonth
        )

        db.collection("goals")
            .document("$userId-$currentMonth")
            .set(goal)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }





}

