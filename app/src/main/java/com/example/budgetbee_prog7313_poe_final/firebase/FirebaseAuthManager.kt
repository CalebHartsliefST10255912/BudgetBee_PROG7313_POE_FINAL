package com.example.budgetbee_prog7313_poe_final.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register a new user
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Login existing user
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }



    // Get current user's UID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Log out the user
    fun logout() {
        auth.signOut()
    }
}
/*
Firestore | Firebase (no date). https://firebase.google.com/docs/firestore.

Add Firebase to your Android project  |  Firebase for Android (no date). https://firebase.google.com/docs/android/setup.

Firebase Authentication (no date). https://firebase.google.com/docs/auth.

Firestore | Firebase (no date). https://firebase.google.com/docs/firestore.

Cloud storage for Firebase (no date). https://firebase.google.com/docs/storage.
 */