package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.fin362.R
import com.example.fin362.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class CardViewAdapter(jobList: List<com.example.fin362.ui.events.Job>, parentActivity: FragmentActivity) : RecyclerView.Adapter<ViewHolder>(){
    private val internalJobList = jobList
    private val internalParentActivity = parentActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recycled_cards, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val job = internalJobList[position]

        holder.itemView.setOnClickListener{
            val jobBundle = Bundle()
            jobBundle.putString("companyName", job.companyName)
            jobBundle.putString("jobTitle", job.positionTitle)
            jobBundle.putString("jobLocation", job.location)
            jobBundle.putString("documentId", job.documentId)
            jobBundle.putString("jobDate", job.dateSaved?.toDate().toString())
            jobBundle.putString("status", job.appStatus)
            jobBundle.putString("link", job.link)

            val fragTransaction = internalParentActivity.supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeDetail(jobBundle))
            fragTransaction.addToBackStack(null)
            fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragTransaction.commit()
        }

        holder.itemView.findViewById<TextView>(R.id.history_job_company_name).text = job.companyName
        holder.itemView.findViewById<TextView>(R.id.history_job_title).text = job.positionTitle
        holder.itemView.findViewById<TextView>(R.id.history_job_location).text = job.location

        val dateFormat = SimpleDateFormat("dd/LL/yy")
        val formattedDate = dateFormat.format(job.dateSaved!!.toDate()).toString()
        holder.itemView.findViewById<TextView>(R.id.history_job_date).text = formattedDate

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
            listSetup(view)
        }
        buttons.getChildAt(1).setOnClickListener {
            viewModel.filterType = "Applied"
            listSetup(view)
        }
        buttons.getChildAt(2).setOnClickListener {
            viewModel.filterType = "Interviewing"
            listSetup(view)
        }
        buttons.getChildAt(3).setOnClickListener {
            viewModel.filterType = "Offer"
            listSetup(view)
        }
        buttons.getChildAt(4).setOnClickListener {
            viewModel.filterType = "Rejected"
            listSetup(view)
        }
    }

    private fun listSetup(view: View){
        viewModel.db.getSavedJobsForUser {
            viewModel.jobs = it

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
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.adapter = CardViewAdapter(viewModel.filteredJobs, requireActivity())
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

        return view
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