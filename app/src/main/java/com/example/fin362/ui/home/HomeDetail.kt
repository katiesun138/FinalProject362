package com.example.fin362.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class HomeDetail(jobBundle: Bundle) : Fragment() {
    private val localBundle = jobBundle

    fun saveChanges(view: View){
        val db = FirebaseDBManager()

        CoroutineScope(Job() + Dispatchers.Default).launch {
            val jobStatus = view.findViewById<TextView>(R.id.history_detail_job_status_text).text.toString()
            val companyName = view.findViewById<EditText>(R.id.history_detail_company_name_text).text.toString()
            val link = view.findViewById<EditText>(R.id.history_detail_link_text).text.toString()
            val location = view.findViewById<EditText>(R.id.history_detail_job_location_text).text.toString()
            val jobTitle = view.findViewById<EditText>(R.id.history_detail_job_title_text).text.toString()

            // An extremely clumsy way of creating a date object from a string
            val dateApplied = Calendar.getInstance()
            val splitDate = view.findViewById<TextView>(R.id.history_detail_job_date_text).text.toString().split("/")
            dateApplied.set(splitDate[2].toInt(), splitDate[1].toInt(), splitDate[0].toInt())

            val timestamp = Timestamp(dateApplied.time)

            db.updateJob(
                localBundle.getString("documentId").toString(),
                jobStatus,
                companyName,
                timestamp,
                null,
                null,
                null,
                null,
                "",
                link,
                location,
                jobTitle
            )
        }
    }

    fun updateStatus(jobStatus: String, view: View){
        val statusBadge = view.findViewById<TextView>(R.id.history_detail_job_status_text)

        if(jobStatus == ""){
            statusBadge.text = "Unknown"
        } else {
            statusBadge.text = jobStatus
        }

        statusBadge.background = when(jobStatus){
            "Applied" ->
                ContextCompat.getDrawable(requireActivity(), R.drawable.history_status_applied)
            "Interviewing" ->
                ContextCompat.getDrawable(requireActivity(), R.drawable.history_status_interviewing)
            "Offer" ->
                ContextCompat.getDrawable(requireActivity(), R.drawable.history_status_offer)
            "Rejected" ->
                ContextCompat.getDrawable(requireActivity(), R.drawable.history_status_rejected)
            else ->
                ContextCompat.getDrawable(requireActivity(), R.drawable.history_status_unknown)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_detail, container, false)

        view.findViewById<EditText>(R.id.history_detail_company_name_text).
            setText(localBundle.getString("companyName"))
        view.findViewById<EditText>(R.id.history_detail_job_title_text).
            setText(localBundle.getString("jobTitle"))
        view.findViewById<EditText>(R.id.history_detail_job_location_text).
            setText(localBundle.getString("jobLocation"))
        view.findViewById<EditText>(R.id.history_detail_link_text).
            setText(localBundle.getString("link"))

        val rawDate = localBundle.getString("jobDate")
        if(rawDate != null) {
            val currentFormat = SimpleDateFormat("EEE LLL dd HH:mm:ss zzz yyyy")
            val targetFormat = SimpleDateFormat("dd/MM/yy")
            val date = currentFormat.parse(rawDate)

            view.findViewById<TextView>(R.id.history_detail_job_date_text).text =
                targetFormat.format(date)
        }

        val jobStatus = localBundle.getString("status")
        if(jobStatus != null){
            updateStatus(jobStatus, view)
        } else {
            updateStatus("", view)
        }

        view.findViewById<CardView>(R.id.history_detail_job_status).setOnClickListener{
            val dialog = HomeStatusDialog()
            dialog.isCancelable

            dialog.show(childFragmentManager, null)

            childFragmentManager.setFragmentResultListener("status", this) { _, bundle ->
                val newStatus = bundle.getString("status")
                if(newStatus != null){
                    updateStatus(newStatus, view)
                }
            }
        }

        view.findViewById<CardView>(R.id.history_detail_date).setOnClickListener{
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(),
                {_, year, month, day ->
                    // Unsure why, but date picker was setting things to one month earlier.
                    // January was month 0, December month 11. This is a workaround.
                    val adjustedMonth = month + 1
                    view.findViewById<TextView>(R.id.history_detail_job_date_text).text =
                        "$day/$adjustedMonth/$year"
                },
                currentYear, currentMonth, currentDay).show()
        }

        view.findViewById<CardView>(R.id.history_detail_save).setOnClickListener{
            saveChanges(view)

            val fragTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.history_container, HomeFragment())
            fragTransaction.addToBackStack(null)
            fragTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            fragTransaction.commit()
        }

        return view
    }
}