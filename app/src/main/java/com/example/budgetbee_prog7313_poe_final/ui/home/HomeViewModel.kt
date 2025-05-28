package com.example.budgetbee_prog7313_poe_final.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.budgetbee_prog7313_poe_final.firebase.FirestoreManager
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    fun loadUserGreeting() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreManager.getUser(uid) { user ->
            if (user != null) {
                _text.postValue("Welcome back, ${user.userName}")
            } else {
                _text.postValue("Welcome!")
            }
        }
    }
}
