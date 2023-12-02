package com.example.fin362.ui.events

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class SavedJobsAdapter(private val context: Context, private val onDeleteClickListener: (position: Int, adapter: SavedJobsAdapter) -> Unit) :
    RecyclerView.Adapter<SavedJobsAdapter.ViewHolder>() {

    private val itemList: MutableList<Job> = mutableListOf()
    val originalList: MutableList<Job> = mutableListOf()
    val firebaseDBManager = FirebaseDBManager()

    //TODO: Hide clearbit company logo api key
    private var clearbitApiKey = "" //clearbit company logo api key

    //cache for logos so we don't have async fetching issues
    private var logoCache = ConcurrentHashMap<String, String?>()

    private var currentQuery: String = ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameTextView)
        val positionTitleTextView: TextView = itemView.findViewById(R.id.positionTitleTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val dateSavedTextView: TextView = itemView.findViewById(R.id.dateSavedTextView)
        val companyLogoImageView: ImageView = itemView.findViewById(R.id.logoImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_entry_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentJob = itemList[position]
        val searchDomain = currentJob.companyName + ".com"
        holder.companyLogoImageView.tag = currentJob.companyName + position

        val logoUrl = logoCache[currentJob.companyName]
        if (logoUrl == "placeholder") {
            holder.companyLogoImageView.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
        } else if (logoUrl != null && logoUrl != "placeholder") {
            // Load the company logo from the cache
            Picasso.get().load(logoUrl).into(holder.companyLogoImageView)
        } else {
            // Fetch the company logo and store the URL in the cache
            fetchCompanyLogo(searchDomain) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null && holder.companyLogoImageView.tag == currentJob.companyName + position) {
                    // Load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(holder.companyLogoImageView)
                    // Cache the logo URL
                    logoCache[currentJob.companyName] = fetchedLogoUrl
                } else if (fetchedLogoUrl == null && holder.companyLogoImageView.tag == currentJob.companyName + position) {
                    // Use the default placeholder drawable
                    holder.companyLogoImageView.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    //prevent accidental overwrite for existing companyNames with logos
                    if (!logoCache.containsKey(currentJob.companyName)) {
                        logoCache[currentJob.companyName] = "placeholder"
                    }
                }
            }
        }

        holder.companyNameTextView.text = currentJob.companyName
        holder.positionTitleTextView.text = currentJob.positionTitle
        holder.locationTextView.text = currentJob.location
        val timestamp: Timestamp? = currentJob.dateSaved
        val formattedDate = timestamp?.toDate()?.let { formatDate(it) }
        holder.dateSavedTextView.text = formattedDate

        holder.itemView.setOnClickListener {
            editJobEntry(position)
        }
    }

    fun setQuery(newQuery: String) {
        currentQuery = newQuery
    }
    override fun getItemCount(): Int = itemList.size

    fun getItem(position: Int): Job? {
        return if (position in 0 until itemList.size) {
            itemList[position]
        } else {
            null
        }
    }
    fun updateData(newList: List<Job>) {
        itemList.clear()
        itemList.addAll(newList)
        originalList.clear()
        originalList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        itemList.clear()

        if (query.isBlank()) {
            // If the query is blank, show the original list
            itemList.addAll(originalList)
        } else {
            // Filter jobs based on the query
            val filteredList = originalList.filter { job ->
                job.companyName.contains(query, ignoreCase = true) ||
                        job.positionTitle.contains(query, ignoreCase = true) ||
                        job.location.contains(query, ignoreCase = true)
            }
            itemList.addAll(filteredList)
        }
        setQuery(query)
        notifyDataSetChanged()
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun fetchCompanyLogo(companyDomain: String, callback: (String?) -> Unit) {
        val apiUrl = "https://logo.clearbit.com/$companyDomain"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $clearbitApiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure (e.g., network issues)
                Handler(context.mainLooper).post {
                    callback(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response
                if (response.isSuccessful) {
                    // Get the URL of the company logo
                    val logoUrl = response.request.url.toString()

                    // Use Handler to post the result back to the main thread
                    Handler(context.mainLooper).post {
                        callback(logoUrl)
                    }
                } else {
                    // Handle non-successful responses
                    Handler(context.mainLooper).post {
                        callback(null)
                    }
                }
            }
        })
    }

    private fun editJobEntry(position: Int) {
        // Get the selected job
        val selectedJob = getItem(position)

        // Create a dialog for updating and saving entry data
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.saved_job_input_form, null)

        // Reference to EditTexts and error message TextView in the dialog
        val companyNameEditText = dialogView.findViewById<EditText>(R.id.company_name_edit_text)
        val positionTitleEditText = dialogView.findViewById<EditText>(R.id.position_title_edit_text)
        val jobTypeEditText = dialogView.findViewById<EditText>(R.id.job_type_edit_text)
        val linkEditText = dialogView.findViewById<EditText>(R.id.link_edit_text)
        val locationEditText = dialogView.findViewById<EditText>(R.id.location_edit_text)
        val errorMessageTextView = dialogView.findViewById<TextView>(R.id.error_message_text_view)

        // Set the existing data to the EditTexts
        companyNameEditText.setText(selectedJob?.companyName)
        positionTitleEditText.setText(selectedJob?.positionTitle)
        jobTypeEditText.setText(selectedJob?.jobType)
        linkEditText.setText(selectedJob?.link)
        locationEditText.setText(selectedJob?.location)

        dialogBuilder.setView(dialogView)
            .setTitle("Update Job Information")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle the cancel button click
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

        saveButton.setOnClickListener{
            // Handle the save button click
            val updatedCompanyName = companyNameEditText.text.toString()
            val updatedPositionTitle = positionTitleEditText.text.toString()
            val updatedJobType = jobTypeEditText.text.toString()
            val updatedLink = linkEditText.text.toString()
            val updatedLocation = locationEditText.text.toString()

            // Update the job entry in the database
            if (selectedJob != null) {
                if (updatedCompanyName.isBlank() || updatedPositionTitle.isBlank() || updatedJobType.isBlank() || updatedLink.isBlank() || updatedLocation.isBlank()) {
                    // Show an error message inside the dialog
                    Log.d("TEST", "Error dialog called")
                    errorMessageTextView.visibility = View.VISIBLE
                    errorMessageTextView.text = "All fields are required"
                } else {
                    firebaseDBManager.updateJob(
                        selectedJob.documentId,
                        selectedJob.appStatus,
                        updatedCompanyName,
                        selectedJob.dateSaved,
                        selectedJob.dateApplied,
                        selectedJob.dateInterview,
                        selectedJob.dateOffer,
                        selectedJob.dateRejected,
                        updatedJobType,
                        updatedLink,
                        updatedLocation,
                        updatedPositionTitle,
                        true
                    )
                    // Refresh the data in the adapter
                    firebaseDBManager.getSavedJobsForUser { jobList ->
                        updateData(jobList)
                        this.filter(currentQuery)
                    }

                    alertDialog.dismiss()
                }
            }
        }
    }
}
