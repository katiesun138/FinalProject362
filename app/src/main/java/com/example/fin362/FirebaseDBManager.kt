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
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDBManager {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Add a new job document to the user's "jobs" collection
    fun saveJob(appStatus: String?, companyName: String, dateSaved: Timestamp?, dateApplied: Timestamp?, dateInterview: Timestamp?,
               dateOffer: Timestamp?, dateRejected: Timestamp?, jobType: String, link: String,
               location: String, positionTitle: String, isSaved: Boolean) {
        currentUser?.let { user ->
            val usersCollection: DocumentReference = firestore.collection("users").document(user.uid)
            val newJobDocument: DocumentReference = usersCollection.collection("jobs").document()

            // Add function to create job before adding or retrieve job and format and store into object
            // set functions to get each timestamp field

            val newJobEntry = hashMapOf(
                "app_status" to appStatus,
                "company_name" to companyName,
                "date_applied" to dateApplied,
                "date_interview" to dateInterview,
                "date_offer" to dateOffer,
                "date_rejected" to dateRejected,
                "date_saved" to dateSaved,
                "is_saved" to isSaved,
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

    fun updateJob(documentId: String, appStatus: String?, companyName: String, dateSaved: Timestamp?, dateApplied: Timestamp?, dateInterview: Timestamp?,
        dateOffer: Timestamp?, dateRejected: Timestamp?, jobType: String, link: String,
        location: String, positionTitle: String, isSaved: Boolean?) {
        currentUser?.let { user ->
            val usersCollection: DocumentReference = firestore.collection("users").document(user.uid)
            val jobDocument: DocumentReference = usersCollection.collection("jobs").document(documentId)

            val updatedJobEntry = hashMapOf(
                "app_status" to appStatus,
                "company_name" to companyName,
                "date_applied" to dateApplied,
                "date_interview" to dateInterview,
                "date_offer" to dateOffer,
                "date_rejected" to dateRejected,
                "date_saved" to dateSaved,
                "is_saved" to isSaved,
                "job_type" to jobType,
                "link" to link,
                "location" to location,
                "position_title" to positionTitle
            )

            jobDocument.update(updatedJobEntry as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("DB", "Job document updated for user: ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("DB", "Error updating job document", e)
                }
        }
    }


    suspend fun deleteJobByPosition(position: Int): Boolean {
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

    suspend fun deleteJobById(documentId: String?): Boolean {
        return suspendCoroutine { continuation ->
            currentUser?.let { user ->
                val usersCollection: DocumentReference =
                    firestore.collection("users").document(user.uid)
                val jobDocumentRef: DocumentReference =
                    usersCollection.collection("jobs").document(documentId!!)

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
                        val dateSaved = document.getTimestamp("date_saved") ?: null
                        val link = document.getString("link") ?: ""
                        val jobType = document.getString("job_type") ?: ""
                        val appStatus = document.getString("app_status") ?: null
                        val dateApplied = document.getTimestamp("date_applied") ?: null
                        val dateInterview = document.getTimestamp("date_interview") ?: null
                        val dateOffer = document.getTimestamp("date_offer") ?: null
                        val dateRejected = document.getTimestamp("date_rejected") ?: null
                        val isSaved = document.getBoolean("is_saved") ?: null

                        val savedJob = Job(documentId, companyName.capitalize(), positionTitle.capitalize(),
                            location.capitalize(), dateSaved, link, jobType, appStatus, dateApplied,
                            dateInterview, dateOffer, dateRejected, isSaved)
                        jobList.add(savedJob)
                    }

                    callback(jobList)
                }
                .addOnFailureListener { exception ->
                    Log.e("DB", "Error getting job documents", exception)
                }
        }
    }

    fun getApplicationJobsForUser(callback: (List<Job>) -> Unit) {
        currentUser?.let { user ->
            val usersCollection: CollectionReference = firestore.collection("users").document(user.uid).collection("jobs")
            val query: Query = usersCollection.whereEqualTo("is_saved", false).orderBy("date_applied", Query.Direction.DESCENDING)

            query.get()
                .addOnSuccessListener { documents ->
                    val jobList = ArrayList<Job>()
                    for (document in documents) {
                        val documentId = document.id
                        val companyName = document.getString("company_name") ?: ""
                        val positionTitle = document.getString("position_title") ?: ""
                        val location = document.getString("location") ?: ""
                        val dateSaved = document.getTimestamp("date_saved") ?: null
                        val link = document.getString("link") ?: ""
                        val jobType = document.getString("job_type") ?: ""
                        val appStatus = document.getString("app_status") ?: null
                        val dateApplied = document.getTimestamp("date_applied") ?: null
                        val dateInterview = document.getTimestamp("date_interview") ?: null
                        val dateOffer = document.getTimestamp("date_offer") ?: null
                        val dateRejected = document.getTimestamp("date_rejected") ?: null
                        val isSaved = document.getBoolean("is_saved") ?: null

                        val savedJob = Job(documentId, companyName.capitalize(), positionTitle.capitalize(),
                            location.capitalize(), dateSaved, link, jobType, appStatus, dateApplied,
                            dateInterview, dateOffer, dateRejected, isSaved)
                        jobList.add(savedJob)
                    }

                    callback(jobList)
                }
                .addOnFailureListener { exception ->
                    Log.e("DB", "Error getting job documents", exception)
                }
        }
    }
    fun saveProfile( name: String,email:String) {
        currentUser?.let { user ->
            val usersCollection: DocumentReference = firestore.collection("users").document(user.uid)
            val newProfileDocument: DocumentReference = usersCollection.collection("profile").document()


            val calendar = Calendar.getInstance()
            val timestamp = Timestamp(calendar.time)

            val newProfileEntry = hashMapOf(
                "name" to name,
                "email" to email

            )

            newProfileDocument.set(newProfileEntry)
                .addOnSuccessListener {
                    val documentId = newProfileDocument.id
                    Log.d("DB", "Profile document added for user: ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("DB", "Error adding profile document", e)
                }
        }
    }
    fun getStatusInformation(callback:(List<String>)->Unit){
        currentUser?.let { user ->
            val usersCollection: CollectionReference = firestore.collection("users").document(user.uid).collection("jobs")
            val query: Query = usersCollection.whereEqualTo("is_saved", false)
            query.get()
                .addOnSuccessListener { documents ->
                    val dataList = ArrayList<String>()
                    var applied: Int =0
                    var interviewing: Int =0
                    var rejected:Int=0
                    var offer:Int=0
                    for (document in documents) {
                        val appStatus = document.getString("app_status") ?: null
                        if (appStatus=="Applied"){
                            applied+=1
                        }
                        if (appStatus=="Interviewing"){
                            interviewing+=1
                        }
                        if (appStatus=="Rejected"){
                            rejected+=1
                        }
                        if (appStatus=="Offer") {
                            offer += 1
                        }

                    }
                    dataList.add(applied.toString())
                    dataList.add(interviewing.toString())
                    dataList.add(rejected.toString())
                    dataList.add(offer.toString())

                    callback(dataList)
                }
                .addOnFailureListener { exception ->
                    Log.e("DB", "Error getting user data", exception)
                }
        }

    }
}
