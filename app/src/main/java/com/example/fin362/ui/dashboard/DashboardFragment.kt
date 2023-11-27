package com.example.fin362.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext as withContext1

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView = binding.textDiscoverJobs
        populateCardView()


//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//            try {
//                getOnlineJobs { result ->
////                    textView.text = result
//                    try {
//                        val jsonObj = JSONObject(result)
//                        val hitsArray = jsonObj.getJSONArray("hits")
//                        var cardContainer = binding.cardHolder
//                        for (i in 0 until hitsArray.length()) {
//                            val hitObject = hitsArray.getJSONObject(i)
//                            val title = hitObject.getString("title")
//                            val company_name = hitObject.getString("company_name")
//                            val formatted_relative_time = hitObject.getString("formatted_relative_time")
//                            val location = hitObject.getString("location")
////                            withContext(Dispatchers.Main) {
//                                // Create a CardView
//                                val cardView =
//                                    inflater.inflate(R.layout.home_recycled_cards, null) as CardView
//
//                                // Find TextViews in the CardView
//                                val historyJobTitle =
//                                    cardView.findViewById<TextView>(R.id.history_job_title)
//                                val historyJobCompanyName =
//                                    cardView.findViewById<TextView>(R.id.history_job_company_name)
//                                val historyJobDate =
//                                    cardView.findViewById<TextView>(R.id.history_job_date)
//                                val historyJobLocation =
//                                    cardView.findViewById<TextView>(R.id.history_job_location)
//
//                                // Set values to the TextViews
//                                historyJobTitle.text = title
//                                historyJobCompanyName.text = company_name
//                                historyJobDate.text = formatted_relative_time
//                                historyJobLocation.text = location
//
//                                // Add the populated CardView to the cardContainer
//                                cardContainer.addView(cardView)
////                            }
//                        }
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                        Log.e("katie", "Error parsing JSON: ${e.message}")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("katie", "error in viewCreate")
//                e.printStackTrace()
//            }
//        }
//
//





//        val profileBtn = binding.imageProfile
//        val filterBtn = binding.iconFilter
//        val cardSelect = binding.cardView1
//
//
//        profileBtn.setOnClickListener(){
//            findNavController().navigate(R.id.nav_from_dash)
//
//        }
//
//        filterBtn.setOnClickListener(){
//            val bottomSheetFragment = DashboardFilterPopup()
//            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
//
//        }
//
//        cardSelect.setOnClickListener(){
//            Log.d("katies", "clicked the card")
//            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
//
//            val newFragment = DashboardDetailed()  // Replace with the actual fragment you want to navigate to
//            fragTransaction.replace(R.id.container, newFragment)
//            fragTransaction.addToBackStack(null)
//            fragTransaction.commit()
//        }



//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    private fun populateCardView() {
        val data = listOf(
            mapOf(
                "company_name" to "Lineage Logistics",
                "formatted_relative_time" to "1 day ago",
                "id" to "2e8f4e2fbf1ff5f1",
                "link" to "/job/2e8f4e2fbf1ff5f1?locality=us",
                "locality" to "us",
                "location" to "Bedford Park, IL 60638",
                "pub_date_ts_milli" to 1698296400000,
                "salary" to mapOf<String, Any>(), // or "salary" to emptyMap()
                "title" to "Operations Manager"
            ),
            mapOf(
                "company_name" to "Sidley Austin LLP",
                "formatted_relative_time" to "3 days ago",
                "id" to "bfe2ad35f90b9e65",
                "link" to "/job/bfe2ad35f90b9e65?locality=us",
                "locality" to "us",
                "location" to "Chicago, IL 60603",
                "pub_date_ts_milli" to 1700632800000,
                "salary" to mapOf<String, Any>(),
                "title" to "Project Manager"
            ),
            mapOf(
                "company_name" to "Michael Page",
                "formatted_relative_time" to "2 days ago",
                "id" to "6a96cd056ba28a1d",
                "link" to "/job/6a96cd056ba28a1d?locality=us",
                "locality" to "us",
                "location" to "Chicago, IL",
                "pub_date_ts_milli" to 1700719200000,
                "salary" to mapOf(
                    "max" to 125000,
                    "min" to 105000,
                    "type" to "yearly"
                ),
                "title" to "Job Title"
            ),
            mapOf(
                "company_name" to "Uncle Julio's",
                "formatted_relative_time" to "Just posted",
                "id" to "24c6f81fba232e48",
                "link" to "/job/24c6f81fba232e48?locality=us",
                "locality" to "us",
                "location" to "Chicago, IL 60642",
                "pub_date_ts_milli" to 1701064800000,
                "salary" to mapOf(
                    "max" to 85000,
                    "min" to 65000,
                    "type" to "yearly"
                ),
                "title" to "Assistant General Manager"
            ),mapOf(
                "company_name" to "M & J Wilkow",
                "formatted_relative_time" to "Today",
                "id" to "c1438a42e8fd1042",
                "link" to "/job/c1438a42e8fd1042?locality=us",
                "locality" to "us",
                "location" to "Chicago, IL 60611",
                "pub_date_ts_milli" to 1700978400000,
                "salary" to mapOf(
                    "max" to 100000,
                    "min" to 80000,
                    "type" to "yearly"
                ),
                "title" to "Assistant General Manager"
            ),
            mapOf(
                "company_name" to "WatsonDwyer Inc.",
                "formatted_relative_time" to "3 days ago",
                "id" to "7b9c96ff78ae82b6",
                "link" to "/job/7b9c96ff78ae82b6?locality=us",
                "locality" to "us",
                "location" to "Chicago, IL",
                "pub_date_ts_milli" to 1700805600000,
                "salary" to mapOf(
                    "max" to 65000,
                    "min" to 65000,
                    "type" to "yearly"
                ),
                "title" to "Mini Office Manager (FT)"
            )
        )

        var cardContainer = binding.cardHolder

        for (i in 0 until 5) {
            Log.d("Katie", "in for loop")
            val inflater = LayoutInflater.from(context)
            val cardView = inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView
            val historyJobTitle = cardView.findViewById<TextView>(R.id.history_job_title)
            val historyJobCompanyName = cardView.findViewById<TextView>(R.id.history_job_company_name)
            val historyJobDate = cardView.findViewById<TextView>(R.id.history_job_date)
            val historyJobLocation = cardView.findViewById<TextView>(R.id.history_job_location)

            // Set values to the TextViews
            historyJobTitle.text = data[i]["title"] as CharSequence?
            historyJobCompanyName.text = data[i]["company_name"] as CharSequence?
            historyJobDate.text = data[i]["formatted_relative_time"] as CharSequence?
            historyJobLocation.text = data[i]["location"] as CharSequence?

            // Add the populated CardView to the cardContainer
            cardContainer.addView(cardView)
        }
    }


    //
//private fun getOnlineJobs(callback:(String?) -> Unit) {
//    try {
//        val client = OkHttpClient()
//
//        val request = Request.Builder()
////            .url("https://indeed12.p.rapidapi.com/jobs/search?query=manager&location=chicago&page_id=1&fromage=3&radius=10")
//            .url("https://www.google.com")
//            .get()
////            .addHeader("Content-Type", "application/json")
////            .addHeader("X-RapidAPI-Key", "b1e1ab1091mshcc9d4bcef44cbf0p1f1095jsn68c0743c6ce6")
////            .addHeader("X-RapidAPI-Host", "indeed12.p.rapidapi.com")
//            .build()
//        Log.d("katie", "Request URL: ${request.url}")
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle failure (e.g., network issues)
//                Log.e("katie", "Network request failed", e)
//                context?.let {
//                    Handler(it.mainLooper).post {
//                        Log.d("katie", "Callback failed")
//                        callback(null)
//                    }
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                Log.d("katie", "in onResponse")
//
//                if (response.isSuccessful) {
//                    // Log the response body
//                    val responseBody = response.body?.string()
//                    Log.d("katie Response Body", responseBody ?: "Empty response body")
//                    context?.let {
//                        Handler(it.mainLooper).post {
//                            Log.d("katie", "Callback success")
//                            callback(responseBody)
//                        }
//                    }
//                } else {
//                    Log.e("katie Network Error", "Unexpected code ${response.code}")
//                    context?.let {
//                        Handler(it.mainLooper).post {
//                            Log.d("katie", "Callback failure")
//                            callback(null)
//                        }
//                    }
//                }
//            }
//        })
    private suspend fun getOnlineJobs(callback: (String) -> Unit) {
        withContext1(Dispatchers.IO) {
            try {
                // Perform the network request and invoke the callback with the result
                val client = OkHttpClient()
//                val url = "https://reqres.in/api/users?page=2"
                val url = "https://indeed12.p.rapidapi.com/jobs/search?query=manager&location=chicago&page_id=1&fromage=3&radius=10"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-RapidAPI-Key", "b1e1ab1091mshcc9d4bcef44cbf0p1f1095jsn68c0743c6ce6")
                    .addHeader("X-RapidAPI-Host", "indeed12.p.rapidapi.com")
                    .build()
                Log.d("katie", "befre newCall")
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("katie", " newCall success")

                        val result = response.body?.string() ?: "Empty response body"
                        callback(result)
                    } else {
                        Log.d("katie", "newCall else statemnet")

                        callback("Error: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.d("katie", "catch in api call")

                callback("Error: ${e.message}")
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}