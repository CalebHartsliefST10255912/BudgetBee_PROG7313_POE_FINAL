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

    fun saveGoal(userId: String, minGoal: Double, maxGoal: Double, onResult: (Boolean) -> Unit) {
        // Build the month string
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthKey = sdf.format(Date())

        // Construct the Goal object
        val goal = Goal(
            minGoal = minGoal,
            maxGoal = maxGoal,
            month   = monthKey
        )

        // Write under users/{userId}/goals/{userId}-{monthKey}
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document("${userId}-$monthKey")
            .set(goal)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun addIncome(userId: String, income: Income, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("incomes")
            .add(income)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getIncomes(userId: String, onResult: (List<Income>) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("incomes")
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObjects(Income::class.java))
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    // LOANS
    fun addLoan(userId: String, loan: Loan, onResult: (Boolean) -> Unit) {
        val docRef = db.collection("users").document(userId).collection("loans").document()
        val loanWithId = loan.copy(id = docRef.id)
        docRef.set(loanWithId)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getLoans(userId: String, onResult: (List<Loan>) -> Unit) {
        db.collection("users").document(userId).collection("loans").get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Loan::class.java))
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun deleteLoan(userId: String, loanId: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(userId).collection("loans").document(loanId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // In FirestoreManager (firebase/FirestoreManager.kt)
    fun updateLoan(userId: String, loan: Loan, onResult: (Boolean) -> Unit) {
        if (loan.id.isBlank()) {
            onResult(false) // no ID → can’t update
            return
        }
        db.collection("users")
            .document(userId)
            .collection("loans")
            .document(loan.id)
            .set(loan)               // overwrite existing doc
            .addOnSuccessListener  { onResult(true) }
            .addOnFailureListener  { onResult(false) }
    }

}

