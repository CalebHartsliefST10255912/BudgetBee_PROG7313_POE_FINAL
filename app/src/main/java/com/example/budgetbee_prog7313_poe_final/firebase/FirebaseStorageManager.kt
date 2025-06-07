package com.example.budgetbee_prog7313_poe_final.firebase

import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*

class FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    companion object {
        private var instance: FirebaseStorageManager? = null
        
        fun getInstance(): FirebaseStorageManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseStorageManager().also { instance = it }
            }
        }
    }

    suspend fun uploadImage(file: File, folder: String): String? {
        try {
            val fileName = UUID.randomUUID().toString()
            val imageRef = storageRef.child("$folder/$fileName")
            
            val uploadTask = imageRef.putFile(file.toUri()).await()
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await()
            return downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun deleteImage(imageUrl: String) {
        try {
            val imageRef = storageRef.storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getImageUrl(imagePath: String): String? {
        try {
            val imageRef = storageRef.child(imagePath)
            return imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
