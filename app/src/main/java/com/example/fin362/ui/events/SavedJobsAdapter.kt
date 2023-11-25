package com.example.fin362.ui.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.fin362.R
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedJobsAdapter(context: Context, resource: Int, objects: List<Job>, private val onDeleteClickListener: (position: Int, adapter: SavedJobsAdapter) -> Unit) :
    ArrayAdapter<Job>(context, resource, objects) {

    var originalList: MutableList<Job> = objects.toMutableList()

    //TODO: Hide clearbit company logo api key
    private var clearbitApiKey = "" //clearbit company logo api key

    //cache for logos so we don't have async fetching issues
    private val logoCache = HashMap<String, String?>()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.saved_entry_view, parent, false)

        clearbitApiKey = context.getString(R.string.clearbit_api_key)

        val currentJob = getItem(position)
        val companyNameTextView: TextView = itemView.findViewById(R.id.companyNameTextView)
        val positionTitleTextView: TextView = itemView.findViewById(R.id.positionTitleTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val dateSavedTextView: TextView = itemView.findViewById(R.id.dateSavedTextView)
        val companyLogoImageView: ImageView = itemView.findViewById(R.id.logoImageView)
        val searchDomain = "www." + currentJob?.companyName.toString() + ".com"

        val logoUrl = logoCache[searchDomain]
        if(logoUrl == "placeholder") {
            companyLogoImageView.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
        } else if (logoUrl != null && logoUrl!= "placeholder") {
            // Load the company logo from the cache
            Picasso.get().load(logoUrl).into(companyLogoImageView)
        } else {
            // Fetch the company logo and store the URL in the cache
            fetchCompanyLogo(searchDomain) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null) {
                    // Load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(companyLogoImageView)
                    // Cache the logo URL
                    logoCache[searchDomain] = fetchedLogoUrl
                } else {
                    // Use the default placeholder drawable
                    companyLogoImageView.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    logoCache[searchDomain] = "placeholder"
                }
            }
        }

        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteIconImageView)
        deleteButton.setOnClickListener {
            // Call the onDeleteClickListener with the position
            onDeleteClickListener.invoke(position, this)
        }

        companyNameTextView.text = currentJob?.companyName
        positionTitleTextView.text = currentJob?.positionTitle
        locationTextView.text = currentJob?.location
        val timestamp: Timestamp? = currentJob?.dateSaved
        val formattedDate = timestamp?.toDate()?.let { formatDate(it) }
        dateSavedTextView.text = formattedDate

        itemView.setOnClickListener {
            // Handle the click event to open the link and redirect in web browser
            val link = currentJob?.link
            if (!link.isNullOrBlank() && Patterns.WEB_URL.matcher(link).matches()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            }
        }

        return itemView
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun updateData(newList: List<Job>) {
        clear()
        addAll(newList)
        notifyDataSetInvalidated()
        notifyDataSetChanged()
        originalList = newList.toMutableList()
    }

    fun extractDomainFromUrl(url: String): String {
        try {
            val uri = URL(url)
            return uri.host
        } catch (e: Exception) {
            // Handle invalid URLs or other exceptions
            return ""
        }
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

    fun filter(query: String) {
        clear()

        if (query.isBlank()) {
            // If the query is blank, show the original list
            addAll(originalList)
        } else {
            // Filter jobs based on the query
            val filteredList = originalList.filter { job ->
                job.companyName.contains(query, ignoreCase = true) ||
                        job.positionTitle.contains(query, ignoreCase = true) ||
                        job.location.contains(query, ignoreCase = true)
            }
            addAll(filteredList)
        }

        notifyDataSetChanged()
    }

}
