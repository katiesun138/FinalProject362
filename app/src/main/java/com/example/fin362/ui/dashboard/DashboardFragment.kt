package com.example.fin362.ui.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext as withContext1

class DashboardFragment : Fragment(), DashboardFilterPopup.FilterPopupListener {

    private var _binding: FragmentDashboardBinding? = null
    private var clearbitApiKey = ""
    private val logoCache = ConcurrentHashMap<String, String?>()


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun onFiltersApplied(jobType: String, location: String, category: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//
        Log.d("katie onfiltersent back", jobType)
            var jobTypeReplace = replaceSpaceWithPercent(jobType)
            var locationReplace = replaceCommaWithExtraPercent(replaceSpaceWithPercent(location))
            var categoryReplace = replaceSpaceWithPercent(category)

            var sendJobType = "level=$jobTypeReplace"
            var sendLocation = "location=$locationReplace"
            var sendCategory = "category=$categoryReplace"

        jobSearchWithQuery(sendJobType, sendLocation, sendCategory)
        }
    }

    fun replaceSpaceWithPercent(input:String):String{
        return input.replace(" ", "%20")
    }

    fun replaceCommaWithExtraPercent(input:String):String{
        return input.replace(",", "%2C")
    }

    data class ApiResponse(
        val page: Int,
        @SerializedName("page_count")
        val pageCount: Int,
        @SerializedName("items_per_page")
        val itemsPerPage: Int,
        val took: Int,
        @SerializedName("timed_out")
        val timedOut: Boolean,
        val total: Int,
        val results: List<Result>
    )

    data class Result(
        val contents: String,
        val name: String,
        val type: String,
        @SerializedName("publication_date")
        val publicationDate: String,
        @SerializedName("short_name")
        val shortName: String,
        @SerializedName("model_type")
        val modelType: String,
        val id: Long,
        val locations: List<Location>,
        val categories: List<Category>,
        val levels: List<Level>,
        val tags: List<String>,
        val refs: Refs,
        val company: Company
    )

    data class Location(
        val name: String
    )

    data class Category(
        val name: String
    )

    data class Level(
        val name: String,
        @SerializedName("short_name")
        val shortName: String
    )

    data class Refs(
        @SerializedName("landing_page")
        val landingPage: String
    )

    data class Company(
        val id: Long,
        @SerializedName("short_name")
        val shortName: String,
        val name: String
    )

    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
    }


    @RequiresApi(Build.VERSION_CODES.O)
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
//        populateCardView()

        if (context?.let { isInternetEnabled(it) } == true) {
            println("Internet is enabled.")
        } else {
            println("Internet is not enabled. Enabling it now...")
            context?.let { enableInternet(it) }
        }
//        var apiResponse: MutableStateFlow<ApiResponse?> = MutableStateFlow(null)

        threadCallJobAPI()

        //enable searchview listener so can parse and generate new jobs
        val filterIcon = binding.iconFilter

        filterIcon.setOnClickListener {

            val overlayView = binding.overlayView

            overlayView.visibility = View.VISIBLE

            val filterPopupFragment = DashboardFilterPopup()
            filterPopupFragment.filterPopupListener = this
            //passing the reference to overlayView id , which is the VIEW that will darken screen
            filterPopupFragment.overlayView = overlayView
            filterPopupFragment.show(
                requireActivity().supportFragmentManager,
                DashboardFilterPopup::class.java.simpleName
            )
        }


        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle query submission
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle query text change
                return true
            }
        })


        val profileBtn = binding.imageProfile

        profileBtn.setOnClickListener(){
            findNavController().navigate(R.id.nav_from_dash)

        }
        return root
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun threadCallJobAPI(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                getOnlineJobs { result ->
                    try {

//                        val jsonString = result.trimIndent();
//                        val gson = Gson()
//
//                        val apiResponse = gson.fromJson(jsonString, ApiResponse::class.java);
//                        val results: List<Result> = apiResponse.results
//
//                        updateUI(results, LayoutInflater.from(requireContext()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("katie", "Error parsing JSON: ${e}")
                    }
                }
            } catch (e: Exception) {
                Log.d("katie", "error in viewCreate")
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun jobSearchWithQuery(jobType: String, location: String, category:String){
        withContext1(Dispatchers.IO) {
            try {
                // Perform the network request and invoke the callback with the result
                var jobTypeSend =""
                var locationSend =""
                var categorySend =""
                val stringList = mutableListOf<String>()

                if (jobType != "level=No%20Preference"){
                    stringList.add(jobType)
                }else{
                    stringList.add(jobTypeSend)
                }
                if (location != "location=No%20Preference"){
                    stringList.add(location)
                }
                else{
                    stringList.add(locationSend)
                }
                if (category != "category=No%20Preference"){
                    stringList.add(category)
                }
                else{
                    stringList.add(categorySend)
                }

                //parse through to add &
                val result = StringBuilder()

                stringList.forEachIndexed { index, item ->
                    if (item != "") {


                        result.append(item)
                        if (index < stringList.size - 1) {
                            result.append("&")
                        }
                    }

                }
                val finalSendURLString = result.toString()



                val client = OkHttpClient()
                val url = "https://www.themuse.com/api/public/jobs?$finalSendURLString&page=1"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .build()
                Log.d("katie", "befre newCall")

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
//                        Log.d("katieSuccess", response.body!!.string())
//                        Log.d("katieSuccesssecond Line", response.peekBody(2048).string())
//
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI(result, LayoutInflater.from(requireContext()))
                        }
//                        val result = Gson().toJson(response.body!!.string())
//                        callback(result)
                    } else {
                        Log.d("katie", "newCall else statemnet")

//                        callback("Error: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.d("katie", "catch in api call")
                Log.d("katieError", e.toString())
//                callback("Error: ${e.message}")
            }
        }
    }




    fun isInternetEnabled(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
        }
    }

    fun enableInternet(context: Context) {
        if (!isInternetEnabled(context)) {
            // If the internet is not enabled, open the wireless settings to enable it
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getOnlineJobs(callback: (String) -> Unit) {
        withContext1(Dispatchers.IO) {
            try {
                // Perform the network request and invoke the callback with the result
                val client = OkHttpClient()
                val url = "https://www.themuse.com/api/public/jobs?location=Vancouver%2C%20Canada&page=1"
//                val url = "https://indeed12.p.rapidapi.com/jobs/search?query=manager&location=chicago&page_id=1&fromage=3&radius=10"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
//                    .addHeader("X-RapidAPI-Key", "b1e1ab1091mshcc9d4bcef44cbf0p1f1095jsn68c0743c6ce6")
//                    .addHeader("X-RapidAPI-Host", "indeed12.p.rapidapi.com")
                    .build()
                Log.d("katie", "befre newCall")
//                Log.d("katie", client.newCall(request).execute().toString())

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
//                        Log.d("katieSuccess", response.body!!.string())
//                        Log.d("katieSuccesssecond Line", response.peekBody(2048).string())
//
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI(result, LayoutInflater.from(requireContext()))
                        }
//                        val result = Gson().toJson(response.body!!.string())
//                        callback(result)
                    } else {
                        Log.d("katie", "newCall else statemnet")

                        callback("Error: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.d("katie", "catch in api call")
                Log.d("katieError", e.toString())
                callback("Error: ${e.message}")
            }
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
                context?.let {
                    Handler(it.mainLooper).post {
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
                    context?.let {
                        Handler(it.mainLooper).post {
                            callback(logoUrl)
                        }
                    }
                } else {
                    // Handle non-successful responses
                    context?.let {
                        Handler(it.mainLooper).post {
                            callback(null)
                        }
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUI(result: String, inflater: LayoutInflater) {
        binding.cardHolder.removeAllViews()
        try{
            val jsonString = result.trimIndent()
            val gson = Gson()

            val apiResponse = gson.fromJson(jsonString, ApiResponse::class.java);
            val results: List<Result> = apiResponse.results
            var cardContainer = binding.cardHolder
            if (results.size == 0){
                binding.textNoResults.visibility = View.VISIBLE
            }
            else {

                binding.textNoResults.visibility = View.GONE
                for (i in 0 until results.size) {
                    val title = results[i].name
                    val company_name = results[i].company.name
                    val instant = Instant.parse(results[i].publicationDate)
                    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val formattedDate = localDateTime.format(dateFormatter)
                    val formatted_relative_time = formattedDate
                    //                            val location = hitObject.getString("location")
                    //                            withContext(Dispatchers.Main) {

                    val cardView =
                        inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView


                    val historyJobTitle = cardView.findViewById<TextView>(R.id.dashJobTitle)
                    val logo = cardView.findViewById<ImageView>(R.id.dashCardLogo)
                    val historyJobCompanyName =
                        cardView.findViewById<TextView>(R.id.dashCompanyName)
                    val historyJobDate = cardView.findViewById<TextView>(R.id.dashJobDate)
                    //                                val historyJobLocation =
                    //                                    cardView.findViewById<TextView>(R.id.history_job_location)

                    fetchCompanyLogo(company_name) { fetchedLogoUrl ->
                        if (fetchedLogoUrl != null) {
                            // Load the company logo
                            Picasso.get().load(fetchedLogoUrl).into(logo)
                            // Cache the logo URL
                            logoCache[company_name] = fetchedLogoUrl
                        } else if (fetchedLogoUrl == null) {
                            // Use the default placeholder drawable
                            logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                            //prevent accidental overwrite for existing companyNames with logos
                            if (!logoCache.containsKey(company_name)) {
                                logoCache[company_name] = "placeholder"
                            }
                        }
                    }

                    // Set values to the TextViews
                    historyJobTitle.text = title
                    historyJobCompanyName.text = company_name
                    historyJobDate.text = formatted_relative_time
                    //                                historyJobLocation.text = location

                    cardView.setOnClickListener() {
                        onCardClick(results[i])
                    }

                    cardContainer.addView(cardView)
                }
            }
        }
        catch(e:Exception){
            binding.textNoResults.visibility = View.VISIBLE
            Log.d("katie error in updateUI", e.toString())
        }

    }

    private fun onCardClick(result: DashboardFragment.Result) {
        val intent = Intent(activity, DashboardDetailedJob::class.java)
        intent.putExtra("jobTitle", result.name);
        intent.putExtra("companyName", result.company.name)
        intent.putExtra("html", result.contents)
        startActivity(intent)


//        val bundle = Bundle()
//        bundle.putString("companyName", result.company.name)
//        bundle.putString("jobTitle", result.name)
////        val action = DashboardFragmentDirections.actionDashboardFragmentToDashboardDetailedFragment(result)
//
//
//        findNavController().navigate(action)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}