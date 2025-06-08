package com.example.budgetbee_prog7313_poe_final.ui.mission

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MissionActivity : AppCompatActivity(){

    val db = FirebaseFirestore.getInstance()


    fun checkAndAssignDailyMissions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        userRef.get().addOnSuccessListener { userDoc ->
            val lastCheck = userDoc.getString("lastMissionCheck")

            if (lastCheck != today) {
                db.collection("missions")
                    .get()
                    .addOnSuccessListener { missionSnapshot ->
                        val allMissions = missionSnapshot.documents.shuffled().take(3)

                        userRef.update(
                            mapOf(
                                "lastMissionCheck" to today,
                                "completedMissions.$today" to emptyList<String>()
                            )
                        )
                    }
            }
        }
    }

}