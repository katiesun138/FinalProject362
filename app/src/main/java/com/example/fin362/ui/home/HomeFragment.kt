package com.example.fin362.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.fin362.R
import com.example.fin362.databinding.FragmentHomeBinding
import com.example.fin362.ui.events.Job
import java.text.SimpleDateFormat

class CardViewAdapter(jobList: List<Job>, parentActivity: FragmentActivity) : RecyclerView.Adapter<ViewHolder>(){
    private val internalJobList = jobList
    private val internalParentActivity = parentActivity
    private val jobCardsCreated = 0
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
            jobBundle.putString("jobDate", job.dateSaved.toDate().toString())
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

        val dateFormat = SimpleDateFormat("LLL dd yyyy")
        val formattedDate = dateFormat.format(job.dateSaved.toDate()).toString()
        holder.itemView.findViewById<TextView>(R.id.history_job_date).text = formattedDate

        //TODO:
        // Once we have a job status option in the database, add another conditional in here
        // to change the job status badge accordingly.
    }

    override fun getItemCount(): Int {
        return internalJobList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }
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

    private fun listSetup(view: View){
        viewModel.db.getSavedJobsForUser {
            viewModel.jobs = it

            // TODO:
            // CREATES DUMMY DATA FOR TESTING
            // REMOVE ME EVENTUALLY
            // Once we have actual data in Firebase, this can probably be removed.
               for(i in 0..10){
                   val time = com.google.firebase.Timestamp(0,0)

                   val job = Job(i.toString(), i.toString(), "!!PLACEHOLDER DATA!!", i.toString(), time, "link")
                   viewModel.jobs += job
               }


            val recyclerView = view.findViewById<RecyclerView>(R.id.history_recycler_container)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = CardViewAdapter(viewModel.jobs, requireActivity())

            Log.d("listsize", viewModel.jobs.size.toString())
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

        view.findViewById<Button>(R.id.history_graph_goto).setOnClickListener{
            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeGraphView())
            fragTransaction.addToBackStack(null)
            fragTransaction.commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listSetup(view)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}