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
    private val defaultNames = listOf(
        "Food","Health","Shopping","Transport","Entertainment",
        "Rent","Gifts","Groceries","Medicine","Savings"
    )

    fun initializeDefaultCategories(userId: String, onResult: (Boolean)->Unit) {
        val cats = defaultNames.map { name ->
            Category(name = name, isDefault = true)
        }
        val coll = db.collection("users").document(userId).collection("categories")
        db.runBatch { b ->
            cats.forEach { b.set(coll.document(), it) }
        }
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun addCustomCategory(userId: String, category: Category, onResult: (Boolean)->Unit) {
        db.collection("users")
            .document(userId)
            .collection("categories")
            .document()
            .set(category.copy(isDefault = false))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getCategories(userId: String, onResult: (List<Category>)->Unit) {
        db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObjects(Category::class.java))
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // EXPENSES
    fun addExpense(userId: String, expense: Expense, onResult: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("expenses").add(expense)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getExpenses(userId: String, onResult: (List<Expense>) -> Unit) {
        db.collection("users").document(userId).collection("expenses").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(Expense::class.java)
                onResult(list)
            }
    }

    // GOALS
    fun setGoal(userId: String, goal: Goal, onResult: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("goals").add(goal)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getGoal(userId: String, onResult: (List<Goal>) -> Unit) {
        db.collection("users").document(userId).collection("goals").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(Goal::class.java)
                onResult(list)
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

