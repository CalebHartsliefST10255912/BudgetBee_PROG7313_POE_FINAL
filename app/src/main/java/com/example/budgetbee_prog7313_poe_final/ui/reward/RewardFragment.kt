package com.example.budgetbee_prog7313_poe_final.ui.reward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Reward
import com.google.firebase.auth.FirebaseAuth

class RewardsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RewardAdapter
    private var userPoints = 0
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rewards, container, false)
        recyclerView = view.findViewById(R.id.rewardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchUserPointsAndRewards()


        return view
    }

    private fun fetchUserPointsAndRewards() {
        FirestoreManager.getUserPoints(userId) { points ->
            userPoints = points
            FirestoreManager.getAvailableRewards { rewards ->
                adapter = RewardAdapter(rewards, userPoints) { reward ->
                    claimReward(reward)
                }
                recyclerView.adapter = adapter
            }
        }
    }

    private fun claimReward(reward: Reward) {
        if (userPoints >= reward.cost) {
            FirestoreManager.claimReward(userId, reward) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Claimed ${reward.title}", Toast.LENGTH_SHORT).show()
                    fetchUserPointsAndRewards()
                } else {
                    Toast.makeText(requireContext(), "Failed to claim reward", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
