package com.example.fin362.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.example.fin362.ui.events.Job
import com.example.fin362.ui.events.SavedJobsAdapter
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class DashboardDetailedJob : AppCompatActivity() {
    val firebaseDBManager = FirebaseDBManager()
    private var clearbitApiKey = ""
    private val logoCache = ConcurrentHashMap<String, String?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_detailed_job)
        val backbtn = findViewById<Button>(R.id.backToDashboard)
        val description = findViewById<TextView>(R.id.jobDescription)
        val companynameRec = intent.getStringExtra("companyName")
        val jobtitleRec = intent.getStringExtra("jobTitle")
        var jobTypeRec = intent.getStringExtra("jobType")
        if (jobTypeRec == null){
            jobTypeRec = "Not Available"
        }
        val jobLinkRec = intent.getStringExtra("jobLink")
        val jobLocationRec = intent.getStringExtra("jobLocation")
        val jobDescriptionRec = intent.getStringExtra("html")
        val savebtn = findViewById<Button>(R.id.saveButton)
        val applybtn = findViewById<Button>(R.id.applyButton)

        if (companynameRec != null) {
            val companyName = findViewById<TextView>(R.id.companyName)
            val jobTitle = findViewById<TextView>(R.id.jobTitle)
            val logo = findViewById<ImageView>(R.id.companyLogo)
            val location = findViewById<TextView>(R.id.jobLocation)
            val jobType = findViewById<TextView>(R.id.jobType)


            companyName.text = companynameRec.toString()
            jobTitle.text = jobtitleRec.toString()
            description.text = jobDescriptionRec?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT) }
            location.text = jobLocationRec.toString()
            jobType.text = jobTypeRec.toString()

            fetchCompanyLogo(companynameRec) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null ) {
                    // load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(logo)
                    // cache the logo URL
                    logoCache[companynameRec] = fetchedLogoUrl
                } else if (fetchedLogoUrl == null) {
                    // wse the default placeholder drawable
                    logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    //prevent accidental overwrite for existing companyNames with logos
                    if (!logoCache.containsKey(companynameRec)) {
                        logoCache[companynameRec] = "placeholder"
                    }
                }
            }
        }

        backbtn.setOnClickListener(){
            super.onBackPressed()
            finish()
        }

        savebtn.setOnClickListener(){
            firebaseDBManager.saveJob(
                null, companynameRec!!, Timestamp.now(),null,
                null, null, null, jobTypeRec!!, jobLinkRec!!,
                jobLocationRec!!, jobtitleRec!!, true
            )
            finish()
        }

        applybtn.setOnClickListener() {
            if (!jobLinkRec.isNullOrBlank() && Patterns.WEB_URL.matcher(jobLinkRec).matches()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jobLinkRec))
                startActivity(intent)
                finish()
            }
        }
    }

    private fun fetchCompanyLogo(companyDomain: String, callback: (String?) -> Unit) {
        val searchDomain = "$companyDomain".substringBefore(" ").replace("[^a-zA-Z0-9]".toRegex(), "")+ ".com"

        val apiUrl = "https://logo.clearbit.com/$searchDomain"


        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer $clearbitApiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure (e.g., network issues)
                this?.let {
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // handle the response
                if (response.isSuccessful) {
                    // get the URL of the company logo
                    val logoUrl = response.request.url.toString()

                    // use Handler to post the result back to the main thread
                    this?.let {
                        Handler(Looper.getMainLooper()).post {
                            callback(logoUrl)
                        }
                    }
                } else {
                    // Handle non-successful responses
                    this?.let {
                        Handler(Looper.getMainLooper()).post {
                            callback(null)
                        }
                    }
                }
            }
        })
    }


}