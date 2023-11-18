package com.example.fin362

import android.util.Log
import com.example.fin362.ui.events.Job
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDBManager {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Add a new job document to the user's "jobs" collection
    fun addJob(job: Any) {
        currentUser?.let { user ->
            val usersCollection: DocumentReference = firestore.collection("users").document(user.uid)
            val newJobDocument: DocumentReference = usersCollection.collection("jobs").document()


            newJobDocument.set(job)
                .addOnSuccessListener {
                    val documentId = newJobDocument.id
                    Log.d("DB", "Job document added for user: ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("DB", "Error adding job document", e)
                }
        }
    }

    suspend fun deleteJob(position: Int): Boolean {
        return suspendCoroutine { continuation ->
            currentUser?.let { user ->
                getSavedJobsForUser { jobList ->
                    if (position in jobList.indices) {
                        val jobToDelete = jobList[position]
                        val usersCollection: DocumentReference =
                            firestore.collection("users").document(user.uid)
                        val jobDocumentRef: DocumentReference =
                            usersCollection.collection("jobs").document(jobToDelete.documentId)

                        // Delete the document
                        jobDocumentRef.delete()
                            .addOnSuccessListener {
                                Log.d("DB", "Job document deleted successfully")
                                continuation.resume(true)
                            }
                            .addOnFailureListener { e ->
                                Log.w("DB", "Error deleting job document", e)
                                continuation.resume(false)
                            }
                    } else {
                        Log.e("DB", "Invalid position")
                        continuation.resume(false)
                    }
                }
            } ?: run {
                // If currentUser is null
                Log.e("DB", "Current user is null")
                continuation.resume(false)
            }
        }
    }


    // Retrieve all job documents for the user
    fun getSavedJobsForUser(callback: (List<Job>) -> Unit) {
        currentUser?.let { user ->
            val usersCollection: CollectionReference = firestore.collection("users").document(user.uid).collection("jobs")
            val query: Query = usersCollection.whereEqualTo("is_saved", true).orderBy("date_applied", Query.Direction.DESCENDING)

            query.get()
                .addOnSuccessListener { documents ->
                    val jobList = ArrayList<Job>()
                    for (document in documents) {
                        val documentId = document.id
                        val companyName = document.getString("company_name") ?: ""
                        val positionTitle = document.getString("position_title") ?: ""
                        val location = document.getString("location") ?: ""
                        val dateSaved = document.getTimestamp("date_saved") ?: Timestamp.now()
                        val link = document.getString("link") ?: ""

                        val savedJob = Job(documentId, companyName, positionTitle, location, dateSaved, link)
                        jobList.add(savedJob)
                    }

                    callback(jobList)
                }
                .addOnFailureListener { exception ->
                    Log.e("DB", "Error getting job documents", exception)
                }
        }
    }
}
