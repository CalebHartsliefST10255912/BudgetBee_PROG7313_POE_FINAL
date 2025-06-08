package com.example.budgetbee_prog7313_poe_final.ui.mission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbee_prog7313_poe_final.R
import com.google.firebase.auth.FirebaseAuth
import com.example.budgetbee_prog7313_poe_final.ui.mission.MissionViewModel

class MissionFragment : Fragment() {

    private lateinit var missionViewModel: MissionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        missionViewModel = ViewModelProvider(this).get(MissionViewModel::class.java)

        val recyclerView: RecyclerView = view.findViewById(R.id.missionRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        missionViewModel.missions.observe(viewLifecycleOwner) { missions ->
            missionViewModel.claimedMissions.observe(viewLifecycleOwner) { claimedMissions ->
                recyclerView.adapter = MissionAdapter(
                    missions = missions,
                    userId = currentUserId,
                    claimedMissions = claimedMissions,
                    onMissionCompleted = {
                        missionViewModel.loadTodaysMissions(currentUserId)
                    }
                )
            }
        }

        missionViewModel.loadTodaysMissions(currentUserId)
    }

}

