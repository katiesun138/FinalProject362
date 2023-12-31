package com.example.fin362.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.withContext as withContext1

class DashboardFragment : Fragment(), DashboardFilterPopup.FilterPopupListener {

    private var _binding: FragmentDashboardBinding? = null
    private var clearbitApiKey = ""
    private val logoCache = ConcurrentHashMap<String, String?>()
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun onFiltersApplied(jobType: String, location: String, category: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            var jobTypeReplace = replaceSpaceWithPercent(jobType)
            var locationReplace = replaceCommaWithExtraPercent(replaceSpaceWithPercent(location))
            var categoryReplace = replaceSpaceWithPercent(category)

            var sendJobType = "level=$jobTypeReplace"
            var sendLocation = "location=$locationReplace"
            var sendCategory = "category=$categoryReplace"

        jobSearchWithQuery(sendJobType, sendLocation, sendCategory)
            getOtherOnlineJob(jobType, location, category)
        }
    }

    fun replaceSpaceWithPercent(input:String):String{
        return input.replace(" ", "%20")
    }

    fun replaceCommaWithExtraPercent(input:String):String{
        return input.replace(",", "%2C")
    }

    data class OtherJobClass(
        val results: List<JobResult>
    )

    data class JobResult(
        val title: String,
        val company: Company2,
        val location: Location2,
        val description: String,
        val contract_type: String,
        val salary_is_predicted: String,
        val redirect_url: String,
        val created: String
    )

    data class Company2(
        val display_name: String
    )

    data class Location2(
        val display_name: String
    )

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

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView = binding.textDiscoverJobs

        if (context?.let { isInternetEnabled(it) } == true) {
            println("Internet is enabled.")
        } else {
            println("Internet is not enabled. Enabling it now...")
            context?.let { enableInternet(it) }
            dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        }

        loadBackupOrNot(inflater)
      

        //filter button is enabled and popup triggered
        val filterIcon = binding.iconFilter

        filterIcon.setOnClickListener {

            val overlayView = binding.overlayView

            overlayView.visibility = View.VISIBLE

            val filterPopupFragment = DashboardFilterPopup()
            filterPopupFragment.filterPopupListener = this
            filterPopupFragment.overlayView = overlayView
            filterPopupFragment.show(
                requireActivity().supportFragmentManager,
                DashboardFilterPopup::class.java.simpleName
            )
        }

        val scrollView: ScrollView = binding.scrollView

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            // Check if the scroll view is at the top
            val isAtTop = scrollView.scrollY == 0
            // Enable or disable swipe refresh layout based on scroll position
            swipeRefreshLayout.isEnabled = isAtTop
        }


        swipeRefreshLayout = binding.swiperefreshlayout
        swipeRefreshLayout.setOnRefreshListener {
            loadBackupOrNot(inflater)
        }


        //search bar for jobs
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchBarSearch(query!!)

                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle query text change
                return true
            }
        })

        //move to profile page
        val profileBtn = binding.imageProfile
        profileBtn.setOnClickListener() {
            findNavController().navigate(R.id.nav_from_dash)

        }
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadBackupOrNot(inflater: LayoutInflater){
        if (::dashboardViewModel.isInitialized) {
            if (dashboardViewModel.jobsResult != null) {
                updateUIWithViewModelData(dashboardViewModel.jobsResult!!, inflater)

                // Check if jobsResult has data
                if (dashboardViewModel.jobsResult!!.isNotEmpty()) {
                    updateUIWithViewModelData2(dashboardViewModel.otherJobsResult!!, inflater, true)
                } else {
                    updateUIWithViewModelData2(dashboardViewModel.otherJobsResult!!, inflater, false)
                }
            } else {
                threadCallJobAPI()
            }
            swipeRefreshLayout.isRefreshing = false

        } else {
            // Initialize the ViewModel if not initialized
            dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
            threadCallJobAPI()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun searchBarSearch(query:String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            getOtherOnlineJobSearch("",query)
        }
    }


    //separate thread created to prepare for API call
    @RequiresApi(Build.VERSION_CODES.O)
    fun threadCallJobAPI(){
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                getOnlineJobs { result ->
                    try {

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                getOtherOnlineJob("","","")
                swipeRefreshLayout.isRefreshing = false

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //API call with queries specified by user
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

                //parse through to add & to url
                val result = StringBuilder()

                stringList.forEachIndexed { index, item ->
                    if (item != "") {


                        result.append(item)
                        if (index < stringList.size - 1) {
                            result.append("&")
                        }
                    }

                }
                //final api url with user queries enabled
                val finalSendURLString = result.toString()

                //api call
                val client = OkHttpClient()
                val url = "https://www.themuse.com/api/public/jobs?$finalSendURLString&page=1"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI(result, LayoutInflater.from(requireContext()))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Error", e.toString())
            }
        }
    }
    

    //check for internet connection - connect if not already
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

    //once onload to page, API function request is called
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getOnlineJobs(callback: (String) -> Unit) {
        withContext1(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val url = "https://www.themuse.com/api/public/jobs?location=Vancouver%2C%20Canada&page=1"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI(result, LayoutInflater.from(requireContext()))
                        }
                    } else {

                        callback("Error: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                callback("Error: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getOtherOnlineJob(type:String, country:String, role:String) {
        withContext1(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                var cCode = "us"
                if (country.contains("Canada")) {
                    cCode = "ca"
                }
                var percentQuery = replaceSpaceWithPercent(type)
                if (!(role.contains("No Preference")) || !(role.contains(""))) {
                    percentQuery = replaceSpaceWithPercent(role)
                }
                var query = "&what=$percentQuery"

                val url = "https://api.adzuna.com/v1/api/jobs/$cCode/search/1?app_id=1c42f8f0&app_key=9c6dc2aeac748a9a7873a6c071931a67$query"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI2(result, LayoutInflater.from(requireContext()))
                        }
                    } else {

                        Log.d("Error:" , response.code.toString())
                    }
                }
            } catch (e: Exception) {
                Log.d("Error: ", e.message.toString())
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getOtherOnlineJobSearch(country:String, role:String) {
        withContext1(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                var cCode = "us"
                if (country.contains("Canada")){
                    cCode = "ca"
                }
                var percentSearch = replaceSpaceWithPercent(role)
                var query = "&what=$percentSearch"
                val url = "https://api.adzuna.com/v1/api/jobs/$cCode/search/1?app_id=1c42f8f0&app_key=9c6dc2aeac748a9a7873a6c071931a67$query"
                val request = Request.Builder().url(url)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use{ response ->
                    if (response.isSuccessful) {
                        val result = response.body!!.string()

                        lifecycleScope.launch(Dispatchers.Main){
                            updateUI2Search(result, LayoutInflater.from(requireContext()))
                        }
                    } else {

                        Log.d("Error:" , response.code.toString())
                    }
                }
            } catch (e: Exception) {
                Log.d("Error: ", e.message.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun populateCardsUI2(result: String, inflater: LayoutInflater){
        try{
            val jsonString = result.trimIndent()
            val gson = Gson()

            val apiResponse = gson.fromJson(jsonString, OtherJobClass::class.java);
            val results = apiResponse.results
            var cardContainer = binding.cardHolder
            dashboardViewModel.otherJobsResult = results


            binding.textNoResults.visibility = View.GONE
            for (i in 0 until results.size) {
                val title = results[i].title
                val company_name = results[i].company.display_name
                val location = results[i].location.display_name
                val instant = Instant.parse(results[i].created)
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                val formattedDate = localDateTime.format(dateFormatter)
                val formatted_relative_time = formattedDate

                val cardView = inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView


                val historyJobTitle = cardView.findViewById<TextView>(R.id.dashJobTitle)
                val logo = cardView.findViewById<ImageView>(R.id.dashCardLogo)
                val historyJobCompanyName = cardView.findViewById<TextView>(R.id.dashCompanyName)
                val historyJobDate = cardView.findViewById<TextView>(R.id.dashJobDate)
                val historyJobLocation = cardView.findViewById<TextView>(R.id.dashJobLocation)

                fetchCompanyLogo(company_name) { fetchedLogoUrl ->
                    if (fetchedLogoUrl != null) {
                        // Load the company logo
                        Picasso.get().load(fetchedLogoUrl).into(logo)
                        // Cache the logo URL
                        try { logoCache[company_name] = fetchedLogoUrl }
                        catch(e: Exception) {}
                    } else if (fetchedLogoUrl == null) {
                        // Use the default placeholder drawable
                        logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                        //prevent accidental overwrite for existing companyNames with logos
                        if (!logoCache.containsKey(company_name)) {
                            try { logoCache[company_name] = "placeholder" }
                            catch(e: Exception){}
                        }
                    }
                }

                // Set values to the TextViews
                historyJobTitle.text = title
                historyJobCompanyName.text = company_name
                historyJobDate.text = formatted_relative_time
                historyJobLocation.text = location

                cardView.setOnClickListener() {
                    onCardClick2(results[i])
                }

                cardContainer.addView(cardView)

            }
        }
        catch(e:Exception){
            binding.textNoResults.visibility = View.VISIBLE
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

    //update ui from the jobs API - populate the job board
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUI(result: String, inflater: LayoutInflater) {
        binding.cardHolder.removeAllViews()
        try {
            val jsonString = result.trimIndent()
            val gson = Gson()

            val apiResponse = gson.fromJson(jsonString, ApiResponse::class.java);
            val results: List<Result> = apiResponse.results
            var cardContainer = binding.cardHolder
            dashboardViewModel.jobsResult = results
            if (results.size == 0) {
                binding.textNoResults.visibility = View.VISIBLE
            } else {
                    binding.textNoResults.visibility = View.GONE
                    for (i in 0 until results.size) {
                        val title = results[i].name
                        val company_name = results[i].company.name
                        val location = results[i].locations[0].name
                        val instant = Instant.parse(results[i].publicationDate)
                        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
                        val formattedDate = localDateTime.format(dateFormatter)
                        val formatted_relative_time = formattedDate

                        val cardView =
                            inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView


                        val historyJobTitle = cardView.findViewById<TextView>(R.id.dashJobTitle)
                        val logo = cardView.findViewById<ImageView>(R.id.dashCardLogo)
                        val historyJobCompanyName =
                            cardView.findViewById<TextView>(R.id.dashCompanyName)
                        val historyJobDate = cardView.findViewById<TextView>(R.id.dashJobDate)
                        val historyJobLocation =
                            cardView.findViewById<TextView>(R.id.dashJobLocation)

                        fetchCompanyLogo(company_name) { fetchedLogoUrl ->
                            if (fetchedLogoUrl != null) {
                                // Load the company logo
                                Picasso.get().load(fetchedLogoUrl).into(logo)
                                // Cache the logo URL
                                try { logoCache[company_name] = fetchedLogoUrl }
                                catch(e: Exception){}
                            } else if (fetchedLogoUrl == null) {
                                // Use the default placeholder drawable
                                logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                                //prevent accidental overwrite for existing companyNames with logos
                                if (!logoCache.containsKey(company_name)) {
                                    try { logoCache[company_name] = "placeholder" }
                                    catch(e: Exception){}
                                }
                            }
                        }


                        // Set values to the TextViews
                        historyJobTitle.text = title
                        historyJobCompanyName.text = company_name
                        historyJobDate.text = formatted_relative_time
                        historyJobLocation.text = location

                        cardView.setOnClickListener() {
                            onCardClick(results[i])
                        }

                        cardContainer.addView(cardView)

                }
            }
        }
        catch(e:Exception){
            binding.textNoResults.visibility = View.VISIBLE
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUI2(result: String, inflater: LayoutInflater) {
        populateCardsUI2(result, inflater)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUI2Search(result: String, inflater: LayoutInflater) {
            binding.cardHolder.removeAllViews()
            populateCardsUI2(result, inflater)

    }

    //function for when a job is clicked - will show the detailed job description
    private fun onCardClick(result: DashboardFragment.Result) {
        val intent = Intent(activity, DashboardDetailedJob::class.java)
        intent.putExtra("jobTitle", result.name);
        intent.putExtra("companyName", result.company.name)
        intent.putExtra("jobType", result.type)
        intent.putExtra("jobLocation", result.locations[0].name)
        intent.putExtra("html", result.contents)
        intent.putExtra("jobLink", result.refs.landingPage)
        startActivity(intent)
    }

    private fun onCardClick2(result: DashboardFragment.JobResult) {
        val intent = Intent(activity, DashboardDetailedJob::class.java)
        intent.putExtra("jobTitle", result.title);
        intent.putExtra("companyName", result.company.display_name)
        intent.putExtra("jobType", result.contract_type)
        intent.putExtra("jobLocation", result.location.display_name)
        intent.putExtra("html", result.description)
        intent.putExtra("jobLink", result.redirect_url)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUIWithViewModelData(jobResult: List<DashboardFragment.Result>,  inflater: LayoutInflater){
        binding.cardHolder.removeAllViews()
        var cardContainer = binding.cardHolder

        for (i in 0 until jobResult.size){
            val title = jobResult[i].name
            val company_name = jobResult[i].company.name
            val location = jobResult[i].locations[0].name
            val instant = Instant.parse(jobResult[i].publicationDate)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
            val formattedDate = localDateTime.format(dateFormatter)
            val formatted_relative_time = formattedDate

            val cardView = inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView


            val historyJobTitle = cardView.findViewById<TextView>(R.id.dashJobTitle)
            val logo = cardView.findViewById<ImageView>(R.id.dashCardLogo)
            val historyJobCompanyName = cardView.findViewById<TextView>(R.id.dashCompanyName)
            val historyJobDate = cardView.findViewById<TextView>(R.id.dashJobDate)
            val historyJobLocation = cardView.findViewById<TextView>(R.id.dashJobLocation)

            fetchCompanyLogo(company_name) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null) {
                    //  the company logo
                    Picasso.get().load(fetchedLogoUrl).into(logo)
                    // caching the logo URL
                    try { logoCache[company_name] = fetchedLogoUrl }
                    catch(e: Exception){}
                } else if (fetchedLogoUrl == null) {
                    //  default placeholder drawable
                    logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    //prevent accidental overwrite for existing companyNames with logos
                    if (!logoCache.containsKey(company_name)) {
                        try { logoCache[company_name] = "placeholder" }
                        catch(e: Exception){}
                    }
                }
            }

            // set values to the TextViews
            historyJobTitle.text = title
            historyJobCompanyName.text = company_name
            historyJobDate.text = formatted_relative_time
            historyJobLocation.text = location

            cardView.setOnClickListener() {
                onCardClick(jobResult[i])
            }

            cardContainer.addView(cardView)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUIWithViewModelData2(results: List<DashboardFragment.JobResult>, inflater: LayoutInflater, existPrev:Boolean){
        if (!existPrev){
            binding.cardHolder.removeAllViews()
        }
        var cardContainer = binding.cardHolder
        for (i in 0 until results.size) {
            val title = results[i].title
            val company_name = results[i].company.display_name
            val location = results[i].location.display_name
            val instant = Instant.parse(results[i].created)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd yyyy")
            val formattedDate = localDateTime.format(dateFormatter)
            val formatted_relative_time = formattedDate

            val cardView =
                inflater.inflate(R.layout.fragment_dashboard_card, null) as CardView


            val historyJobTitle = cardView.findViewById<TextView>(R.id.dashJobTitle)
            val logo = cardView.findViewById<ImageView>(R.id.dashCardLogo)
            val historyJobCompanyName =
                cardView.findViewById<TextView>(R.id.dashCompanyName)
            val historyJobDate = cardView.findViewById<TextView>(R.id.dashJobDate)
            val historyJobLocation =
                cardView.findViewById<TextView>(R.id.dashJobLocation)

            fetchCompanyLogo(company_name) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null) {
                    // Load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(logo)
                    // Cache the logo URL
                    try{ logoCache[company_name] = fetchedLogoUrl }
                    catch(e: Exception){}
                } else if (fetchedLogoUrl == null) {
                    // Use the default placeholder drawable
                    logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    //prevent accidental overwrite for existing companyNames with logos
                    if (!logoCache.containsKey(company_name)) {
                        try{ logoCache[company_name] = "placeholder" }
                        catch(e: Exception){}
                    }
                }
            }


            // Set values to the TextViews
            historyJobTitle.text = title
            historyJobCompanyName.text = company_name
            historyJobDate.text = formatted_relative_time
            historyJobLocation.text = location

            cardView.setOnClickListener() {
                onCardClick2(results[i])
            }

            cardContainer.addView(cardView)

        }


    }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}