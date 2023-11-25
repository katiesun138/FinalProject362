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
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fin362.FirebaseDBManager
import com.example.fin362.FirebaseUIActivity
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding
import com.example.fin362.databinding.FragmentEventsBinding
import com.example.fin362.ui.dashboard.DashboardViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


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

        //setup custom savedJobsAdapter
        val savedJobsAdapter = SavedJobsAdapter(requireContext(), R.layout.saved_entry_view, ArrayList(),
            onDeleteClickListener = { position, adapter ->
                GlobalScope.launch(Dispatchers.Main) {
                    val deleteResult = firebaseDBManager.deleteJob(position)
                    if (deleteResult) {
                        withContext(Dispatchers.IO) {
                            firebaseDBManager.getSavedJobsForUser { jobList ->
                                adapter.clear()
                                adapter.addAll(jobList)
                                adapter.notifyDataSetInvalidated()
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
            }})
        eventsListView.adapter = savedJobsAdapter

        // Retrieve all savedJobs
        firebaseDBManager.getSavedJobsForUser { jobList ->
            savedJobsAdapter.updateData(jobList)
        }

        addButton.setOnClickListener {
            // Create an AlertDialog with an EditText for each parameter
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.saved_job_input_form, null)
            dialogBuilder.setView(dialogView)

            // Reference to EditTexts in the dialog
            val companyNameEditText = dialogView.findViewById<EditText>(R.id.companyNameEditText)
            val positionTitleEditText = dialogView.findViewById<EditText>(R.id.positionTitleEditText)
            val jobTypeEditText = dialogView.findViewById<EditText>(R.id.jobTypeEditText)
            val linkEditText = dialogView.findViewById<EditText>(R.id.linkEditText)
            val locationEditText = dialogView.findViewById<EditText>(R.id.locationEditText)
            // Add more EditText references for other parameters

            dialogBuilder.setTitle("Enter Job Information")
            dialogBuilder.setPositiveButton("Save") { dialog, _ ->
                val companyName = companyNameEditText.text.toString()
                val positionTitle = positionTitleEditText.text.toString()
                val jobType = jobTypeEditText.text.toString()
                val link = linkEditText.text.toString()
                val location = locationEditText.text.toString()

                // Call saveJob function with user input
                firebaseDBManager.saveJob(null, companyName, null,
                    null, null, null, jobType, link,
                    location, positionTitle)

                // Re-query to get the job just added to the adapter listView
                firebaseDBManager.getSavedJobsForUser { jobList ->
                    savedJobsAdapter.updateData(jobList)
                }
            }

            dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                // Do nothing or add any other action on cancel
                dialog.cancel()
            }

            val alertDialog = dialogBuilder.create()
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
            firebaseDBManager.getSavedJobsForUser{jobList ->
                savedJobsAdapter.updateData(jobList)
            }
            swipeRefreshLayout.isRefreshing = false
        }

        return eventView
    }
}