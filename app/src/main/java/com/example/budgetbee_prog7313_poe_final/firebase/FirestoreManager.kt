package com.example.budgetbee_prog7313_poe_final.firebase

import com.example.budgetbee_prog7313_poe_final.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.budgetbee_prog7313_poe_final.model.Goal
import com.google.firebase.auth.FirebaseAuth
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

    // MISSIONS
    fun addMission(mission: Mission, onResult: (Boolean) -> Unit) {
        db.collection("missions").document(mission.id).set(mission)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getAllMissions(onResult: (List<Mission>) -> Unit) {
        db.collection("missions").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Mission::class.java) }
                onResult(list)
            }
    }

    fun completeMission(userId: String, missionId: String, onResult: (Boolean) -> Unit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val field = "completedMissions.$today"

        db.collection("users").document(userId)
            .update(field, FieldValue.arrayUnion(missionId))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun updateLastLoginMissionClaimTime(userId: String, onResult: (Boolean) -> Unit) {
        val now = Timestamp.now()
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("lastLoginMissionClaim", now)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getLastLoginMissionClaimTime(userId: String, onResult: (Date?) -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val timestamp = snapshot.getTimestamp("lastLoginMissionClaim")
                onResult(timestamp?.toDate())
            }
            .addOnFailureListener {
                onResult(null)
            }
    }



    //POINTS
    fun getUserPoints(userId: String, onResult: (Point?) -> Unit) {
        db.collection("points").document(userId).get()
            .addOnSuccessListener { snapshot ->
                val points = snapshot.toObject(Point::class.java)
                onResult(points)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateHoneyPoints(userId: String, amount: Int, onResult: (Boolean) -> Unit) {
        db.collection("points").document(userId)
            .update("honeyPoints", FieldValue.increment(amount.toLong()))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { error ->
                db.collection("points").document(userId)
                    .set(Point(honeyPoints = amount))
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
    }

    //REWARDS
    fun addReward(reward: Reward, onResult: (Boolean) -> Unit) {
        db.collection("rewards").document(reward.id).set(reward)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getAvailableRewards(onResult: (List<Reward>) -> Unit) {
        db.collection("rewards")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val rewards = snapshot.documents.mapNotNull { it.toObject(Reward::class.java) }
                onResult(rewards)
            }
    }




}

