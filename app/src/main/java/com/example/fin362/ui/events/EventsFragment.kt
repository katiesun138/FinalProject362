package com.example.fin362.ui.events

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class Job(
    val documentId: String,
    val companyName: String,
    val positionTitle: String,
    val location: String,
    val dateSaved: Timestamp?,
    val link: String,
    val jobType: String,
    val appStatus: String?,
    val dateApplied: Timestamp?,
    val dateInterview: Timestamp?,
    val dateOffer: Timestamp?,
    val dateRejected: Timestamp?,
    val isSaved: Boolean?
)

class EventsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val firebaseDBManager = FirebaseDBManager()

    private lateinit var searchBar: SearchView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventView = inflater.inflate(R.layout.fragment_events, container, false)
        searchBar = eventView.findViewById(R.id.search_view)
        searchBar.queryHint = "Search..."
        // Initialize firebase database
        auth = FirebaseAuth.getInstance()

        val eventsRecyclerView: RecyclerView = eventView.findViewById(R.id.event_list)
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val savedJobsAdapter = SavedJobsAdapter(requireContext(), onDeleteClickListener={ position, adapter ->
            GlobalScope.launch(Dispatchers.Main) {
                val deleteResult =
                    firebaseDBManager.deleteJobById(adapter.getItem(position)?.documentId)
                if (deleteResult) {
                    withContext(Dispatchers.IO) {
                        firebaseDBManager.getSavedJobsForUser { jobList ->
                            val deletedItemId = adapter.getItem(position)?.documentId
                            // Remove the item from the originalList based on the document ID
                            adapter.originalList.removeAll { it.documentId == deletedItemId }
                            adapter.updateData(jobList)
                            adapter.filter(searchBar.query.toString())
                        }
                    }
                }
            }
        })

        eventsRecyclerView.adapter = savedJobsAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not used for swipe-to-delete
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val jobEntry = savedJobsAdapter.getItem(position)

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) {
                                // Use GlobalScope for the background task
                                val deleteResult = GlobalScope.async {
                                    firebaseDBManager.deleteJobById(jobEntry?.documentId)
                                }.await()

                                if (deleteResult) {
                                    firebaseDBManager.getSavedJobsForUser { jobList ->
                                        savedJobsAdapter.updateData(jobList)
                                        // Update adapter data on the main thread
                                        savedJobsAdapter.originalList.removeAll { it.documentId == jobEntry?.documentId }
                                        savedJobsAdapter.updateData(jobList)
                                        savedJobsAdapter.filter(searchBar.query.toString())
                                    }

                                    showUndoSnackbar(jobEntry, position, eventView, savedJobsAdapter, searchBar)
                                }
                            }
                        }
                    }
                    ItemTouchHelper.RIGHT -> {
                        val jobLink = savedJobsAdapter.getItem(position)?.link
                        if (!jobLink.isNullOrBlank() && Patterns.WEB_URL.matcher(jobLink).matches()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jobLink))
                            context?.startActivity(intent)
                            eventsRecyclerView.adapter?.notifyItemChanged(position)
                            showReturnToAppDialog(savedJobsAdapter, jobEntry, position)
                        } else {
                            // Reset the item's position to simulate bounce back
                            eventsRecyclerView.adapter?.notifyItemChanged(position)
                            Toast.makeText(context, "No link added for this job", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(eventsRecyclerView)

        // Retrieve all savedJobs
        firebaseDBManager.getSavedJobsForUser { jobList ->
            savedJobsAdapter.updateData(jobList)
        }

        // make entire search bar clickable
        searchBar.setOnClickListener(View.OnClickListener { searchBar.isIconified = false })

        // set up search bar filtering on query change
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

        val addButton: FloatingActionButton = eventView.findViewById(R.id.event_add_button)
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

                        val calendar = Calendar.getInstance()
                        val dateSavedTimestamp = Timestamp(calendar.time)

                        firebaseDBManager.saveJob(
                            null, companyName, dateSavedTimestamp,null,
                            null, null, null, jobType, link,
                            location, positionTitle, true
                        )

                        firebaseDBManager.getSavedJobsForUser { jobList ->
                            savedJobsAdapter.updateData(jobList)
                            savedJobsAdapter.filter(searchBar.query.toString())
                        }

                        alertDialog.dismiss()
                    }
                }
            }
            alertDialog.show()
        }

        val swipeRefreshLayout: SwipeRefreshLayout = eventView.findViewById(R.id.event_swipeLayout)

        eventsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}

            override fun onScrolled(
                recyclerView: RecyclerView, dx: Int, dy: Int
            ) {
                val firstVisibleItemPosition =
                    (eventsRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val topRowVerticalPosition = if (eventsRecyclerView.childCount == 0 || firstVisibleItemPosition == 0) {
                    0
                } else {
                    eventsRecyclerView.getChildAt(0).top
                }
                swipeRefreshLayout.isEnabled = firstVisibleItemPosition == 0 && topRowVerticalPosition >= 0
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            firebaseDBManager.getSavedJobsForUser { jobList ->
                savedJobsAdapter.updateData(jobList)
                savedJobsAdapter.filter(searchBar.query.toString())
            }
            swipeRefreshLayout.isRefreshing = false
        }

        return eventView
    }

    fun showUndoSnackbar(deletedItem: Job?, position: Int, eventView: View, savedJobsAdapter: SavedJobsAdapter, searchBar: SearchView) {
        deletedItem?.let {
            val snackbar = Snackbar.make(
                eventView,
                "Job deleted",
                Snackbar.LENGTH_LONG
            )
            snackbar.setAction("Undo") {
                // Handle the "Undo" action
                firebaseDBManager.saveJob(
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
                    true
                )

                firebaseDBManager.getSavedJobsForUser { jobList ->
                    savedJobsAdapter.updateData(jobList)
                    savedJobsAdapter.filter(searchBar.query.toString())
                }

            }
            snackbar.show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Clear the search query when the fragment is resumed
        searchBar.setQuery("", false)
    }

    private fun showReturnToAppDialog(savedJobsAdapter: SavedJobsAdapter, jobEntry: Job?, position: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setMessage("Did you apply?")
            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                // Handle the OK button click if needed
                Log.d("TEST", jobEntry.toString() + position)

                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        // Use GlobalScope for the background task
                        val deleteResult = GlobalScope.async {
                            if (jobEntry != null) {
                                firebaseDBManager.updateJob(jobEntry.documentId, "Applied", jobEntry.companyName, jobEntry.dateSaved, Timestamp.now(), jobEntry.dateInterview, jobEntry.dateOffer,
                                    jobEntry.dateRejected, jobEntry.jobType, jobEntry.link, jobEntry.location, jobEntry.positionTitle, false)
                            }
                        }.await()

                        if (deleteResult != null){
                            firebaseDBManager.getSavedJobsForUser { jobList ->
                                savedJobsAdapter.updateData(jobList)
                                // Update adapter data on the main thread
                                savedJobsAdapter.originalList.removeAll { it.documentId == jobEntry?.documentId }
                                savedJobsAdapter.updateData(jobList)
                                savedJobsAdapter.filter(searchBar.query.toString())
                            }
                        }
                    }}


            }
            alertDialogBuilder.setNegativeButton("No") {
                    _,_ ->
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }, 2000L)
    }

}
