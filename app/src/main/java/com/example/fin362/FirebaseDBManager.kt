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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDBManager {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Add a new job document to the user's "jobs" collection
    fun saveJob(appStatus: String?, companyName: String, dateApplied: String?, dateInterview: String?,
               dateOffer: String?, dateRejected: String?, jobType: String, link: String,
               location: String, positionTitle: String) {
        currentUser?.let { user ->
            val usersCollection: DocumentReference = firestore.collection("users").document(user.uid)
            val newJobDocument: DocumentReference = usersCollection.collection("jobs").document()

            // Add function to create job before adding or retrieve job and format and store into object
            // set functions to get each timestamp field
            val calendar = Calendar.getInstance()
            val timestamp = Timestamp(calendar.time)

            val newJobEntry = hashMapOf(
                "app_status" to appStatus,
                "company_name" to companyName,
                "date_applied" to dateApplied,
                "date_interview" to dateInterview,
                "date_offer" to dateOffer,
                "date_rejected" to dateRejected,
                "date_saved" to timestamp,
                "is_saved" to true,
                "job_type" to jobType,
                "link" to link,
                "location" to location,
                "position_title" to positionTitle
            )

            newJobDocument.set(newJobEntry)
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
            val query: Query = usersCollection.whereEqualTo("is_saved", true).orderBy("date_saved", Query.Direction.DESCENDING)

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

                        val savedJob = Job(documentId, companyName.capitalize(), positionTitle.capitalize(), location.capitalize(), dateSaved, link)
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
