package com.example.budgetbee_prog7313_poe_final.firebase

import android.util.Log
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
import com.google.firebase.firestore.Query
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
    }
      
    // LOANS
    fun addLoan(userId: String, loan: Loan, onResult: (Boolean) -> Unit) {
        val docRef = db.collection("users").document(userId).collection("loans").document()
        val loanWithId = loan.copy(id = docRef.id)
        docRef.set(loanWithId)
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

    fun getUserPoints(userId: String, callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val pointsDocRef = db.collection("points").document(userId)

        pointsDocRef.get()
            .addOnSuccessListener { document ->
                val points = document.getLong("honeyPoints")?.toInt() ?: 0
                callback(points)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Failed to get user points", e)
                callback(0)
            }
    }



    //REWARDS
    fun addReward(reward: Reward, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val rewardRef = db.collection("rewards").document()

        val rewardData = hashMapOf(
            "id" to rewardRef.id,
            "title" to reward.title,
            "description" to reward.description,
            "cost" to reward.cost,
            "currency" to reward.currency,
            "imageUrl" to reward.imageUrl,
            "isActive" to reward.isActive
        )

        rewardRef.set(rewardData)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    fun getAvailableRewards(callback: (List<Reward>) -> Unit) {
        FirebaseFirestore.getInstance().collection("rewards")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { result ->
                val rewards = result.documents.mapNotNull { doc ->
                    doc.toObject(Reward::class.java)?.copy(id = doc.id)
                }
                Log.d("FirestoreManager", "Available rewards fetched: ${rewards.size}")
                callback(rewards)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreManager", "Failed to fetch rewards: ${e.message}", e)
                callback(emptyList())
            }
    }


    fun claimReward(userId: String, reward: Reward, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val pointsDocRef = db.collection("points").document(userId)
        val claimedRewardsRef = db.collection("users")
            .document(userId)
            .collection("claimedRewards")

        db.runTransaction { transaction ->
            val pointsSnapshot = transaction.get(pointsDocRef)
            val currentPoints = pointsSnapshot.getLong("honeyPoints") ?: 0

            if (currentPoints < reward.cost) {
                throw Exception("Not enough points to claim reward")
            }

            transaction.update(pointsDocRef, "honeyPoints", currentPoints - reward.cost)

            val claimData = hashMapOf(
                "rewardId" to reward.id,
                "title" to reward.title,
                "description" to reward.description,
                "cost" to reward.cost,
                "currency" to reward.currency,
                "imageUrl" to reward.imageUrl,
                "claimedAt" to com.google.firebase.Timestamp.now(),
                "barcode" to UUID.randomUUID().toString().substring(0, 8)
            )

            val claimedDocRef = claimedRewardsRef.document()
            transaction.set(claimedDocRef, claimData)
        }.addOnSuccessListener {
            Log.d("FirestoreManager", "Reward claimed successfully")
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("FirestoreManager", "Failed to claim reward", e)
            callback(false)
        }
    }


    fun getClaimedRewards(userId: String, callback: (List<Reward>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .collection("claimedRewards")
            .orderBy("claimedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val rewards = result.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        Reward(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            cost = (data["cost"] as? Long)?.toInt() ?: 0,
                            currency = data["currency"] as? String ?: "honeyPoints",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            isActive = true
                        )
                    } else null
                }
                callback(rewards)
            }
            .addOnFailureListener {
                callback(emptyList())
            }   
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


/*
Firestore | Firebase (no date). https://firebase.google.com/docs/firestore.

Add Firebase to your Android project  |  Firebase for Android (no date). https://firebase.google.com/docs/android/setup.

Firebase Authentication (no date). https://firebase.google.com/docs/auth.

Firestore | Firebase (no date). https://firebase.google.com/docs/firestore.

Cloud storage for Firebase (no date). https://firebase.google.com/docs/storage.
 */









