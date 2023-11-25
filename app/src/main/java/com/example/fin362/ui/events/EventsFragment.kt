package com.example.fin362.ui.events

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class Job(
    val documentId: String,
    val companyName: String,
    val positionTitle: String,
    val location: String,
    val dateSaved: Timestamp,
    val link: String
)

class EventsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventView = inflater.inflate(R.layout.fragment_events, container, false)

        // Initialize firebase database
        val firebaseDBManager = FirebaseDBManager()
        auth = Firebase.auth

        // Get currentUser ID
        val currentUser = auth.currentUser
        val swipeRefreshLayout: SwipeRefreshLayout = eventView.findViewById(R.id.event_swipeLayout)
        val eventsListView: ListView = eventView.findViewById(R.id.event_list)
        val addButton: Button = eventView.findViewById(R.id.event_add_button)
        val searchBar: SearchView = eventView.findViewById(R.id.search_view)
        searchBar.queryHint = "Search..."

        //setup custom savedJobsAdapter
        val savedJobsAdapter = SavedJobsAdapter(requireContext(), R.layout.saved_entry_view, ArrayList(),
            onDeleteClickListener = { position, adapter ->
                GlobalScope.launch(Dispatchers.Main) {
                    val deleteResult = firebaseDBManager.deleteJob(position)
                    if (deleteResult) {
                        withContext(Dispatchers.IO) {
                            firebaseDBManager.getSavedJobsForUser { jobList ->
                                adapter.originalList.removeAt(position)
                                adapter.updateData(jobList)
                                adapter.filter(searchBar.query.toString())
                            }
                        }
                    }
            }})
        eventsListView.adapter = savedJobsAdapter

        // Retrieve all savedJobs
        firebaseDBManager.getSavedJobsForUser { jobList ->
            savedJobsAdapter.updateData(jobList)
        }

        // make entire searchbar clickable
        searchBar.setOnClickListener(View.OnClickListener { searchBar.setIconified(false) })

        //set up searchbar filtering on query change
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle submission if needed
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the list based on the search query
                savedJobsAdapter.filter(newText.orEmpty())
                return true
            }
        })

        addButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.saved_job_input_form, null)
            dialogBuilder.setView(dialogView)

            // Reference to EditTexts and error message TextView in the dialog
            val companyNameEditText = dialogView.findViewById<EditText>(R.id.company_name_edit_text)
            val positionTitleEditText = dialogView.findViewById<EditText>(R.id.position_title_edit_text)
            val jobTypeEditText = dialogView.findViewById<EditText>(R.id.job_type_edit_text)
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
                    val link = linkEditText.text.toString()
                    val location = locationEditText.text.toString()

                    // Validate input fields
                    if (companyName.isBlank() || positionTitle.isBlank() || jobType.isBlank() || link.isBlank() || location.isBlank()) {
                        // Show an error message inside the dialog
                        errorMessageTextView.visibility = View.VISIBLE
                        errorMessageTextView.text = "All fields are required"
                    } else {
                        // Call saveJob function with user input
                        firebaseDBManager.saveJob(null, companyName, null,
                            null, null, null, jobType, link,
                            location, positionTitle)

                        firebaseDBManager.getSavedJobsForUser { jobList ->
                            savedJobsAdapter.updateData(jobList)
                        }

                        alertDialog.dismiss()
                    }
                }
            }
            alertDialog.show()
        }

        // swipeRefreshLayout was not designed to support listView, so we need to enable refresh
        // only when listView's top position is in view, otherwise we get odd behavior scrolling both
        // source: https://stackoverflow.com/questions/32030998/cant-go-up-in-listview-because-swiperefreshlayout
        eventsListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                val topRowVerticalPosition =
                    if (eventsListView == null || eventsListView.getChildCount() === 0) 0 else eventsListView.getChildAt(
                        0
                    ).getTop()
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0)
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            val currentFilterQuery = searchBar.query.toString()

            firebaseDBManager.getSavedJobsForUser{jobList ->
                savedJobsAdapter.updateData(jobList)
                savedJobsAdapter.filter(currentFilterQuery)
            }
            swipeRefreshLayout.isRefreshing = false
        }

        return eventView
    }
}