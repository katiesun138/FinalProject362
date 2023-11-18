package com.example.fin362.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
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

        // Add function to create job before adding or retrieve job and format and store into object
        // set functions to get each timestamp field
        val calendar = Calendar.getInstance()
        //calendar.set(2023, Calendar.NOVEMBER, 19, 0, 30)
        val timestamp = Timestamp(calendar.time)
        val job = hashMapOf(
            "app_status" to "Interviewing",
            "company_name" to "Stripe",
            "date_applied" to timestamp,
            "date_interview" to null,
            "date_offer" to null,
            "date_rejected" to null,
            "date_saved" to null,
            "is_saved" to true,
            "job_type" to "Internship",
            "link" to "https://www.metacareers.com/v2/jobs/1007975740551656/",
            "location" to "New York, NY",
            "notes" to "some notes for meta",
            "position_title" to "Software Engineer"
        )

        // Retrieve all savedJobs
        firebaseDBManager.getSavedJobsForUser{jobList ->
            savedJobsAdapter.updateData(jobList)
        }

        // Add new job listing to saved section
        addButton.setOnClickListener {
            firebaseDBManager.addJob(job)

            //re-query to get the job just added to adapter listView
            firebaseDBManager.getSavedJobsForUser{jobList ->
                savedJobsAdapter.updateData(jobList)
            }
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