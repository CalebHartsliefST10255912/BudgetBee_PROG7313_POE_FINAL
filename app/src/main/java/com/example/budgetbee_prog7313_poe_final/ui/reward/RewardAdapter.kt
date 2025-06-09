package com.example.budgetbee_prog7313_poe_final.ui.reward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Reward

class RewardAdapter(
    private val rewards: List<Reward>,
    private val userPoints: Int,
    private val onClaim: ((Reward) -> Unit)? = null
) : RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {


    inner class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.rewardTitle)
        val description: TextView = itemView.findViewById(R.id.rewardDescription)
        val cost: TextView = itemView.findViewById(R.id.rewardCost)
        val claimButton: Button = itemView.findViewById(R.id.claimRewardButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewards[position]
        holder.title.text = reward.title
        holder.description.text = reward.description
        holder.cost.text = "Cost: ${reward.cost} ${reward.currency}"

        if (onClaim != null && reward.isActive && userPoints >= reward.cost) {
            holder.claimButton.isEnabled = true
            holder.claimButton.text = "Claim"
            holder.claimButton.setOnClickListener {
                holder.claimButton.isEnabled = false
                onClaim.invoke(reward)
            }
        } else {
            holder.claimButton.isEnabled = false
            holder.claimButton.text = "Claimed"
        }

    }

    override fun getItemCount(): Int = rewards.size
}
