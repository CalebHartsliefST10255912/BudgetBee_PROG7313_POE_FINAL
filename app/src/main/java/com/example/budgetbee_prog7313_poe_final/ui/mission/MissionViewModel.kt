package com.example.budgetbee_prog7313_poe_final.ui.mission

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Mission
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MissionViewModel : ViewModel() {
    private val _missions = MutableLiveData<List<Mission>>()
    val missions: LiveData<List<Mission>> get() = _missions

    private val _claimedMissions = MutableLiveData<Set<String>>()
    val claimedMissions: LiveData<Set<String>> get() = _claimedMissions

    private val _loginMissionCooldown = MutableLiveData<Long>()
    val loginMissionCooldown: LiveData<Long> get() = _loginMissionCooldown



    fun loadMissions() {
        FirestoreManager.getAllMissions { missionList ->
            _missions.postValue(missionList)
        }

    }

    fun loadTodaysMissions(userId: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        FirestoreManager.getAllMissions { allMissions ->
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { userSnapshot ->
                    val completed = userSnapshot.get("completedMissions.$today") as? List<String> ?: emptyList()
                    val lastClaimTimestamp = userSnapshot.getTimestamp("lastLoginMissionClaim")?.toDate()

                    val claimedMissions = completed.toSet()

                    val now = Date()
                    val waitMillis = 24 * 60 * 60 * 1000L
                    val remainingTime = if (lastClaimTimestamp != null) {
                        waitMillis - (now.time - lastClaimTimestamp.time)
                    } else {
                        0L
                    }

                    _missions.value = allMissions
                    _claimedMissions.value = claimedMissions
                    _loginMissionCooldown.value = maxOf(0L, remainingTime)
                }
        }
    }

}

