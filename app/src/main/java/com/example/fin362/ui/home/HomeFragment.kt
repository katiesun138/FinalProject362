package com.example.fin362.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.fin362.R
import com.example.fin362.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class CardViewAdapter(jobList: List<com.example.fin362.ui.events.Job>, parentActivity: FragmentActivity) : RecyclerView.Adapter<ViewHolder>(){
    private val internalJobList = jobList
    private val internalParentActivity = parentActivity
    private val jobBundle = Bundle()
    // Code for getting logos copied from SavedJobsAdapter.kt
    // TODO: This will need to be changed eventually
    private var clearbitApiKey = ""
    private val logoCache = ConcurrentHashMap<String, String?>()
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
                Handler(internalParentActivity.mainLooper).post {
                    callback(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response
                if (response.isSuccessful) {
                    // Get the URL of the company logo
                    val logoUrl = response.request.url.toString()

                    // Use Handler to post the result back to the main thread
                    Handler(internalParentActivity.mainLooper).post {
                        callback(logoUrl)
                    }
                } else {
                    // Handle non-successful responses
                    Handler(internalParentActivity.mainLooper).post {
                        callback(null)
                    }
                }
            }
        })
    }

    private fun getLogos(currentJob: com.example.fin362.ui.events.Job, holder: RecyclerView.ViewHolder, position: Int) {
        val logo = holder.itemView.findViewById<ImageView>(R.id.history_job_logo)
        logo.tag = currentJob.companyName + position
        val logoUrl = logoCache[currentJob.companyName]

        // We do a cute trick here with placing the URL into the holder's tag
        // Trying to do it in other ways will result in timing issues, as the onClickListener is
        // set up immediately, while we can't actually know the URL until this function resolves.
        // Putting it in the tag lets us both set up the listener instantly while still having
        // the logo actually work as expected.
        //
        // If the button is clicked instantly, before this function resolves, the logo on the
        // detail page will just be blank, but that's fine.
        if (logoUrl == "placeholder") {
            logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
            holder.itemView.tag = "placeholder"
        } else if (logoUrl != null && logoUrl != "placeholder") {
            // Load the company logo from the cache
            Picasso.get().load(logoUrl).into(logo)
            holder.itemView.tag = logoUrl
        } else {
            val searchDomain = currentJob.companyName + ".com"
            // Fetch the company logo and store the URL in the cache
            fetchCompanyLogo(searchDomain) { fetchedLogoUrl ->
                if (fetchedLogoUrl != null && logo.tag == currentJob.companyName + position) {
                    // Load the company logo
                    Picasso.get().load(fetchedLogoUrl).into(logo)
                    // Cache the logo URL
                    logoCache[currentJob.companyName] = fetchedLogoUrl

                    holder.itemView.tag = fetchedLogoUrl
                } else if (fetchedLogoUrl == null && logo.tag == currentJob.companyName + position) {
                    // Use the default placeholder drawable
                    logo.setImageResource(R.drawable.ic_company_placeholder_black_24dp)
                    //prevent accidental overwrite for existing companyNames with logos
                    if (!logoCache.containsKey(currentJob.companyName)) {
                        logoCache[currentJob.companyName] = "placeholder"
                    }

                    holder.itemView.tag = "placeholder"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recycled_cards, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val job = internalJobList[position]

        getLogos(job, holder, position)

        holder.itemView.setOnClickListener{
            jobBundle.putString("companyName", job.companyName)
            jobBundle.putString("jobTitle", job.positionTitle)
            jobBundle.putString("jobLocation", job.location)
            jobBundle.putString("jobType", job.jobType)
            jobBundle.putString("documentId", job.documentId)
            jobBundle.putString("savedDate", job.dateSaved?.toDate().toString())
            jobBundle.putString("appliedDate", job.dateApplied?.toDate().toString())
            jobBundle.putString("interviewDate", job.dateInterview?.toDate().toString())
            jobBundle.putString("offerDate", job.dateOffer?.toDate().toString())
            jobBundle.putString("rejectedDate", job.dateRejected?.toDate().toString())
            jobBundle.putString("status", job.appStatus)
            jobBundle.putString("link", job.link)
            jobBundle.putString("logoUrl", holder.itemView.tag.toString())

            val fragTransaction = internalParentActivity.supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeDetail(jobBundle))
            fragTransaction.addToBackStack(null)
            fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragTransaction.commit()
        }

        holder.itemView.findViewById<TextView>(R.id.history_job_company_name).text = job.companyName
        holder.itemView.findViewById<TextView>(R.id.history_job_title).text = job.positionTitle
        holder.itemView.findViewById<TextView>(R.id.history_job_location).text = job.location

        val dateView = holder.itemView.findViewById<TextView>(R.id.history_job_date)
        var displayedDate: Timestamp? = Timestamp.now()
        when (job.appStatus) {
            "Applied" -> {
                displayedDate = job.dateApplied
            }
            "Interviewing" -> {
                displayedDate =  job.dateInterview
            }
            "Offer" -> {
                displayedDate = job.dateOffer
            }
                "Rejected" -> {
                    displayedDate = job.dateRejected
                }
            }

        if (displayedDate != null) {
            val outputFormat = SimpleDateFormat("MMM d, yyyy")
            val date = Date(displayedDate.seconds * 1000 + displayedDate.nanoseconds / 1000000)
            val displayDate = outputFormat.format(date)
            dateView.text = displayDate
        } else {
            //Current status has no set date yet
            dateView.text  = "N/A"
        }

        val statusBadge = holder.itemView.findViewById<TextView>(R.id.history_job_status)
        statusBadge.background = when(job.appStatus){
            "Applied" ->
                ContextCompat.getDrawable(internalParentActivity, R.drawable.history_status_applied)
            "Interviewing" ->
                ContextCompat.getDrawable(internalParentActivity, R.drawable.history_status_interviewing)
            "Offer" ->
                ContextCompat.getDrawable(internalParentActivity, R.drawable.history_status_offer)
            "Rejected" ->
                ContextCompat.getDrawable(internalParentActivity, R.drawable.history_status_rejected)
            else ->
                ContextCompat.getDrawable(internalParentActivity, R.drawable.history_status_unknown)
        }

        if(job.appStatus.isNullOrBlank()){
            statusBadge.text = "Unknown"
        } else {
            statusBadge.text = job.appStatus
        }
    }

    override fun getItemCount(): Int {
        return internalJobList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private fun spinnerSetup(view: View) {
        val layoutSpinner = view.findViewById<Spinner>(R.id.history_view_spinner)
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.history_layout_types))

        layoutSpinner.adapter = adapter

        layoutSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Intentionally does nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> viewModel.compactView = false
                    1 -> viewModel.compactView = true
                    else -> viewModel.compactView = false // Shouldn't be possible
                }
                //TODO: Make this actually change anything on the display
            }
        }
    }

    private fun filterSetup(view: View){
        val buttons = view.findViewById<LinearLayout>(R.id.history_filter_buttons)

        // No good way to loop through this. Even if we set the filterType to a number,
        // doing some "filterType = i" thing would lead to incorrect results
        buttons.getChildAt(0).setOnClickListener {
            viewModel.filterType = ""
            listSetupFiltered(view)
        }
        buttons.getChildAt(1).setOnClickListener {
            viewModel.filterType = "Applied"
            listSetupFiltered(view)
        }
        buttons.getChildAt(2).setOnClickListener {
            viewModel.filterType = "Interviewing"
            listSetupFiltered(view)
        }
        buttons.getChildAt(3).setOnClickListener {
            viewModel.filterType = "Offer"
            listSetupFiltered(view)
        }
        buttons.getChildAt(4).setOnClickListener {
            viewModel.filterType = "Rejected"
            listSetupFiltered(view)
        }
    }

    private fun listSetupFiltered(view: View){
        // Clear the filtered list first
        viewModel.filteredJobs = listOf()
        // Then repopulate it
        for(i in 0..<viewModel.jobs.size){
            if (viewModel.filterType == viewModel.jobs[i].appStatus ||
                viewModel.filterType == "") { // Blank filter type is 'all'
                viewModel.filteredJobs += viewModel.jobs[i]
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.history_recycler_container)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CardViewAdapter(viewModel.filteredJobs, requireActivity())
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun listSetup(view: View){
        viewModel.db.getApplicationJobsForUser {
            viewModel.jobs = it.toMutableList()

            val recyclerView = view.findViewById<RecyclerView>(R.id.history_recycler_container)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = CardViewAdapter(viewModel.jobs, requireActivity())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root

        spinnerSetup(view)
        filterSetup(view)

        view.findViewById<Button>(R.id.history_graph_goto).setOnClickListener{
            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeGraphView(viewModel.jobs))
            fragTransaction.addToBackStack(null)
            fragTransaction.commit()
        }

        val addButton: FloatingActionButton = view.findViewById(R.id.history_add_button)
        addButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.application_job_input_form, null)
            dialogBuilder.setView(dialogView)

            // Reference to EditTexts and error message TextView in the dialog
            val companyNameEditText = dialogView.findViewById<EditText>(R.id.company_name_edit_text)
            val positionTitleEditText = dialogView.findViewById<EditText>(R.id.position_title_edit_text)
            val jobTypeEditText = dialogView.findViewById<EditText>(R.id.job_type_edit_text)
            val jobStatusSpinner = dialogView.findViewById<Spinner>(R.id.job_status_spinner)
            val linkEditText = dialogView.findViewById<EditText>(R.id.link_edit_text)
            val locationEditText = dialogView.findViewById<EditText>(R.id.location_edit_text)
            val errorMessageTextView = dialogView.findViewById<TextView>(R.id.error_message_text_view)

            dialogBuilder.setTitle("Enter Job Information")
            dialogBuilder.setPositiveButton("Save", null) // Set to null initially to override the automatic dismiss

            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = dialogBuilder.create()

            // Override the positive button click to perform custom logic
            alertDialog.setOnShowListener {
                val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.setOnClickListener {
                    val companyName = companyNameEditText.text.toString()
                    val positionTitle = positionTitleEditText.text.toString()
                    val jobType = jobTypeEditText.text.toString()
                    val jobStatus = jobStatusSpinner.selectedItem.toString()
                    val link = linkEditText.text.toString()
                    val location = locationEditText.text.toString()

                    // Validate input fields
                    if (companyName.isBlank() || positionTitle.isBlank() || jobType.isBlank() || link.isBlank() || location.isBlank()) {
                        // Show an error message inside the dialog
                        errorMessageTextView.visibility = View.VISIBLE
                        errorMessageTextView.text = "All fields are required"
                    } else {
                        // Call saveJob function with user input
                        var appliedDate: Timestamp? = null
                        var interviewDate: Timestamp? = null
                        var offerDate: Timestamp? = null
                        var rejectedDate: Timestamp? = null
                        when (jobStatus) {
                            "Applied" -> {
                                appliedDate = Timestamp.now()
                            }
                            "Interviewing" -> {
                                interviewDate =  Timestamp.now()
                            }
                            "Offer" -> {
                                offerDate = Timestamp.now()
                            }
                            "Rejected" -> {
                                rejectedDate = Timestamp.now()
                            }
                        }

                        viewModel.db.saveJob(
                            jobStatus, companyName, null, appliedDate,
                            interviewDate, offerDate, rejectedDate, jobType, link,
                            location, positionTitle, false
                        )
                        listSetup(view)
                        alertDialog.dismiss()
                    }
                }
            }
            alertDialog.show()
        }

        val historyRecyclerView: RecyclerView = view.findViewById(R.id.history_recycler_container)
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val cardViewAdapter = CardViewAdapter(viewModel.jobs, requireActivity())

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                target: ViewHolder
            ): Boolean {
                // Not used for swipe-to-delete
                return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val jobEntry = viewModel.jobs[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) {
                                // Use GlobalScope for the background task
                                val deleteResult = GlobalScope.async {
                                    viewModel.db.deleteJobById(jobEntry?.documentId)
                                }.await()
                                val removedItem = viewModel.jobs.removeAt(position)
                                if (deleteResult) {
                                    listSetup(view)
                                    showUndoSnackbar(jobEntry, position, view, cardViewAdapter, removedItem)
                                }
                            }
                        }
                    }
                    ItemTouchHelper.RIGHT -> {
                        val jobLink = viewModel.jobs[position]?.link
                        if (!jobLink.isNullOrBlank() && Patterns.WEB_URL.matcher(jobLink).matches()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jobLink))
                            context?.startActivity(intent)
                            historyRecyclerView.adapter?.notifyItemChanged(position)
                        } else {
                            // Reset the item's position to simulate bounce back
                            historyRecyclerView.adapter?.notifyItemChanged(position)
                            Toast.makeText(context, "No link added for this job", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(historyRecyclerView)

        return view
    }

    fun showUndoSnackbar(deletedItem: com.example.fin362.ui.events.Job?, position: Int, view: View, savedJobsAdapter: CardViewAdapter, removedItem: com.example.fin362.ui.events.Job) {
        deletedItem?.let {
            val snackbar = Snackbar.make(
                view,
                "Job deleted",
                Snackbar.LENGTH_LONG
            )
            snackbar.setAction("Undo") {
                // Handle the "Undo" action
                viewModel.db.saveJob(
                    deletedItem.appStatus,
                    deletedItem.companyName,
                    deletedItem.dateSaved,
                    deletedItem.dateApplied,
                    deletedItem.dateInterview,
                    deletedItem.dateOffer,
                    deletedItem.dateRejected,
                    deletedItem.jobType,
                    deletedItem.link,
                    deletedItem.location,
                    deletedItem.positionTitle,
                    false
                )
                viewModel.jobs.add(position, removedItem)
                listSetup(view)

            }
            snackbar.show()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // List setup all goes onto its own thread.
        // Can't do anything with it until database responds, which is the first step of the setup.
        // Don't want to lock up everything until it responds!
        CoroutineScope(Job() + Dispatchers.Default).launch {
            listSetup(view)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}