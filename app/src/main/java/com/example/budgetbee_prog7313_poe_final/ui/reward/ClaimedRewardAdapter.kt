package com.example.budgetbee_prog7313_poe_final.ui.reward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.model.Reward

class ClaimedRewardAdapter(
    private val rewards: List<Reward>
) : RecyclerView.Adapter<ClaimedRewardAdapter.ClaimedRewardViewHolder>() {

    inner class ClaimedRewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.rewardTitle)
        val description: TextView = itemView.findViewById(R.id.rewardDescription)
        val cost: TextView = itemView.findViewById(R.id.rewardCost)
        val barcodeImage: ImageView = itemView.findViewById(R.id.barcodeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaimedRewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_claimed_reward, parent, false)
        return ClaimedRewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClaimedRewardViewHolder, position: Int) {
        val reward = rewards[position]
        holder.title.text = reward.title
        holder.description.text = reward.description
        holder.cost.text = "Cost: ${reward.cost} ${reward.currency}"
        holder.barcodeImage.setImageResource(R.drawable.fake_barcode)
    }

    override fun getItemCount(): Int = rewards.size
}
