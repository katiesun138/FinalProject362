package com.example.fin362.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.example.fin362.R
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class DashboardDetailedJob : AppCompatActivity() {

    private var clearbitApiKey = ""
    private val logoCache = ConcurrentHashMap<String, String?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_detailed_job)
        val backbtn = findViewById<Button>(R.id.backToDashboard)
        val description = findViewById<TextView>(R.id.jobDescription)
        val companynameRec = intent.getStringExtra("companyName")
        val jobtitleRec = intent.getStringExtra("jobTitle")
        val jobDescriptionRec = intent.getStringExtra("html")

        if (companynameRec != null) {
            Log.d("katie value in dashboard detailed job", companynameRec)
            val companyName = findViewById<TextView>(R.id.companyName)
            val jobTitle = findViewById<TextView>(R.id.jobTitle)
            val logo = findViewById<ImageView>(R.id.companyLogo)

            companyName.text = companynameRec.toString()
            jobTitle.text = jobtitleRec.toString()

            description.text = jobDescriptionRec?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT) }


            fetchCompanyLogo(companynameRec) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null ) {
                    // Load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(logo)
                    // Cache the logo URL
                    logoCache[companynameRec] = fetchedLogoUrl
                } else if (fetchedLogoUrl == null) {
                    // Use the default placeholder drawable
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

    }


    private fun fetchCompanyLogo(companyDomain: String, callback: (String?) -> Unit) {
        val searchDomain = "www." + companyDomain + ".com"

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
                // Handle the response
                if (response.isSuccessful) {
                    // Get the URL of the company logo
                    val logoUrl = response.request.url.toString()

                    // Use Handler to post the result back to the main thread
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