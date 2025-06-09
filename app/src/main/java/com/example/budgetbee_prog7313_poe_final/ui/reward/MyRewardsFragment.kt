package com.example.budgetbee_prog7313_poe_final.ui.reward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.example.budgetbee_prog7313_poe_final.model.Reward
import com.google.firebase.auth.FirebaseAuth

class MyRewardsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClaimedRewardAdapter
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_rewards, container, false)
        recyclerView = view.findViewById(R.id.myRewardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        FirestoreManager.getClaimedRewards(userId) { rewards ->
            adapter = ClaimedRewardAdapter(rewards)
            recyclerView.adapter = adapter
        }

        return view
    }
}

