package com.example.medisight.data.repository

import android.net.Uri
import android.util.Log
import com.example.medisight.data.model.ScanHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MedicalHistoryRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun saveHistory(
        imageUri: Uri,
        scanResult: String,
        confidence: Float,
        onComplete: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        val timestamp = System.currentTimeMillis()
        val historyId = database.child("scan_history").push().key ?: run {
            onComplete(false)
            return
        }

        val imageRef = storage.child("scans/$userId/$historyId.jpg")

        imageRef.putFile(imageUri)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d("Upload", "Upload is $progress% done")
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                val history = ScanHistory(
                    id = historyId,
                    userId = userId,
                    imageUrl = downloadUrl.toString(),
                    scanResult = scanResult,
                    timestamp = timestamp,
                    confidence = confidence
                )

                database.child("scan_history")
                    .child(historyId)
                    .setValue(history.toMap())
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getHistoryForUser(): Flow<List<ScanHistory>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow

        val historyRef = database.child("scan_history")
            .orderByChild("userId")
            .equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val histories = mutableListOf<ScanHistory>()
                for (childSnapshot in snapshot.children) {
                    val history = ScanHistory(
                        id = childSnapshot.child("id").getValue(String::class.java) ?: "",
                        userId = childSnapshot.child("userId").getValue(String::class.java) ?: "",
                        imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)
                            ?: "",
                        scanResult = childSnapshot.child("scanResult").getValue(String::class.java)
                            ?: "",
                        timestamp = childSnapshot.child("timestamp").getValue(Long::class.java)
                            ?: 0L,
                        confidence = childSnapshot.child("confidence").getValue(Float::class.java)
                            ?: 0f
                    )
                    histories.add(history)
                }
                histories.sortByDescending { it.timestamp }
                trySend(histories)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        historyRef.addValueEventListener(listener)
        awaitClose { historyRef.removeEventListener(listener) }
    }

    fun deleteHistory(historyId: String, onComplete: (Boolean) -> Unit) {
        database.child("scan_history")
            .child(historyId)
            .get()
            .addOnSuccessListener { snapshot ->
                val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)
                if (imageUrl != null) {
                    // Delete image from Storage
                    val imageRef = storage.storage.getReferenceFromUrl(imageUrl)
                    imageRef.delete().addOnCompleteListener { imageTask ->
                        if (imageTask.isSuccessful) {
                            // Delete data from Realtime Database
                            database.child("scan_history")
                                .child(historyId)
                                .removeValue()
                                .addOnSuccessListener { onComplete(true) }
                                .addOnFailureListener { onComplete(false) }
                        } else {
                            onComplete(false)
                        }
                    }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}