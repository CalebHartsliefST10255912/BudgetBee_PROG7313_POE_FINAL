package com.example.budgetbee_prog7313_poe_final.ui.mission

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Mission
import java.util.Date

class MissionAdapter(
    private val missions: List<Mission>,
    private val userId: String,
    private val claimedMissions: Set<String>,
    private val onMissionCompleted: () -> Unit
) : RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

    inner class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.missionTitle)
        val desc: TextView = itemView.findViewById(R.id.missionDesc)
        val points: TextView = itemView.findViewById(R.id.missionPoints)
        val claimButton: Button = itemView.findViewById(R.id.claimButton)
        val cooldownText: TextView = itemView.findViewById(R.id.cooldownText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val mission = missions[position]
        holder.title.text = mission.title
        holder.desc.text = mission.description
        holder.points.text = "Reward: ${mission.points} points"

        val isClaimed = claimedMissions.contains(mission.id)


        if (mission.condition == "login") {
            holder.claimButton.text = "Checking..."
            holder.claimButton.isEnabled = false

            FirestoreManager.getLastLoginMissionClaimTime(userId) { lastClaimDate ->
                val now = Date()
                val waitMillis = 24 * 60 * 60 * 1000L // 24 hours

                if (lastClaimDate == null || now.time - lastClaimDate.time >= waitMillis) {
                    if (isClaimed) {
                        holder.claimButton.text = "Claimed"
                        holder.claimButton.isEnabled = false
                    } else {
                        holder.claimButton.text = "Claim"
                        holder.claimButton.isEnabled = true
                        holder.claimButton.setOnClickListener {
                            FirestoreManager.completeMission(userId, mission.id) { success ->
                                if (success) {
                                    FirestoreManager.updateHoneyPoints(userId, mission.points) { updated ->
                                        if (updated) {
                                            FirestoreManager.updateLastLoginMissionClaimTime(userId) { updated ->
                                                if (updated) {
                                                    holder.claimButton.text = "Claimed"
                                                    holder.claimButton.isEnabled = false
                                                    onMissionCompleted()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val timeLeftMillis = waitMillis - (now.time - lastClaimDate.time)
                    val hours = timeLeftMillis / (1000 * 60 * 60)
                    val minutes = (timeLeftMillis / (1000 * 60)) % 60
                    val seconds = (timeLeftMillis / 1000) % 60

                    holder.claimButton.text = "Unavailable"
                    holder.claimButton.isEnabled = false
                    holder.cooldownText.text = String.format("Available in %02dh %02dm %02ds", hours, minutes, seconds)
                    holder.cooldownText.visibility = View.VISIBLE
                }
            }
            return
        }

        if (mission.condition == "test") {
            holder.claimButton.text = "Claim"
            holder.claimButton.isEnabled = true
            holder.cooldownText.visibility = View.GONE
            holder.claimButton.setOnClickListener {
                FirestoreManager.completeMission(userId, mission.id) { success ->
                    if (success) {
                        FirestoreManager.updateHoneyPoints(userId, mission.points) { updated ->
                            if (updated) {
                                onMissionCompleted()
                            }
                        }
                    }
                }
            }
            return
        }

        if (isClaimed) {
            holder.claimButton.text = "Claimed"
            holder.claimButton.isEnabled = false
        } else {
            holder.claimButton.text = "Claim"
            holder.claimButton.isEnabled = true
            holder.cooldownText.visibility = View.GONE
            holder.claimButton.setOnClickListener {
                FirestoreManager.completeMission(userId, mission.id) { success ->
                    if (success) {
                        FirestoreManager.updateHoneyPoints(userId, mission.points) { updated ->
                            if (updated) {
                                holder.claimButton.text = "Claimed"
                                holder.claimButton.isEnabled = false
                                onMissionCompleted()
                            }
                        }
                    }
                }
            }
        }
    }


    override fun getItemCount(): Int = missions.size
}
