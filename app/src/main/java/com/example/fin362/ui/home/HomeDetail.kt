package com.example.fin362.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.fin362.FirebaseDBManager
import com.example.fin362.R
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HomeDetail(jobBundle: Bundle) : Fragment() {
    private val localBundle = jobBundle

    val monthMap = mapOf(
        "Jan." to 1,
        "Feb." to 2,
        "Mar." to 3,
        "Apr." to 4,
        "May" to 5,
        "Jun." to 6,
        "Jul." to 7,
        "Aug." to 8,
        "Sep." to 9,
        "Oct." to 10,
        "Nov." to 11,
        "Dec." to 12
    )

    fun saveChanges(view: View){
        val db = FirebaseDBManager()

        CoroutineScope(Job() + Dispatchers.Default).launch {
            val jobStatus = view.findViewById<TextView>(R.id.history_detail_job_status_text).text.toString()
            val companyName = view.findViewById<EditText>(R.id.history_detail_company_name_text).text.toString()
            val link = view.findViewById<EditText>(R.id.history_detail_link_text).text.toString()
            val location = view.findViewById<EditText>(R.id.history_detail_job_location_text).text.toString()
            val jobTitle = view.findViewById<EditText>(R.id.history_detail_job_title_text).text.toString()
            val jobType = localBundle.getString("jobType")

            // An extremely clumsy way of creating a date object from a string
            val cal = Calendar.getInstance()
            val splitDate = view.findViewById<TextView>(R.id.history_detail_job_date_text).text.toString()

            val regex = Regex("""(\w{3}\.) (\d+), (\d{4})""")
            val matchResult = regex.find(splitDate)

            if (matchResult != null) {
                val (monthStr, dayStr, yearStr) = matchResult.destructured
                val month = monthMap[monthStr]?.minus(1)
                val day = dayStr.toInt()
                val year = yearStr.toInt()

                val resultArray = arrayOf(day, month!!, year)
                cal.set(resultArray[2], resultArray[1], resultArray[0])
            }

            val updatedDate = Timestamp(cal.time)

            val savedDate: Timestamp? = getTimestampFromBundle(localBundle, "savedDate")
            var appliedDate: Timestamp? = getTimestampFromBundle(localBundle, "appliedDate")
            var interviewDate: Timestamp? = getTimestampFromBundle(localBundle, "interviewDate")
            var offerDate: Timestamp? = getTimestampFromBundle(localBundle, "offerDate")
            var rejectedDate: Timestamp? = getTimestampFromBundle(localBundle, "rejectedDate")

            //based on the current jobStatus set the user updated date for that jobStatus
            when (jobStatus) {
                "Applied" -> {
                    appliedDate = updatedDate
                }
                "Interviewing" -> {
                    interviewDate = updatedDate
                }
                "Offer" -> {
                    offerDate = updatedDate
                }
                "Rejected" -> {
                    rejectedDate = updatedDate
                }
            }

            db.updateJob(
                localBundle.getString("documentId").toString(),
                jobStatus,
                companyName,
                savedDate,
                appliedDate,
                interviewDate,
                offerDate,
                rejectedDate,
                jobType!!,
                link,
                location,
                jobTitle,
                false
            )
        }
    }

    private fun updateStatus(jobStatus: String, view: View){
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
        updateDisplayedDate(jobStatus, view)
    }


    private fun getTimestampFromBundle(bundle: Bundle?, key: String): Timestamp? {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
        return bundle?.getString(key)?.let {
            try {
                Timestamp(Date(dateFormat.parse(it).time))
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun updateDisplayedDate(jobStatus: String?, view: View){
        val dateView = view.findViewById<TextView>(R.id.history_detail_job_date_text)
        var displayedDate: Timestamp? = Timestamp.now()
        when (jobStatus) {
            "Applied" -> {
                displayedDate = getTimestampFromBundle(localBundle, "appliedDate")
            }
            "Interviewing" -> {
                displayedDate =  getTimestampFromBundle(localBundle, "interviewDate")
            }
            "Offer" -> {
                displayedDate =  getTimestampFromBundle(localBundle, "offerDate")
            }
            "Rejected" -> {
                displayedDate =  getTimestampFromBundle(localBundle, "rejectedDate")
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_detail, container, false)

        val logo = view.findViewById<ImageView>(R.id.history_detail_logo)
        val logoUrl = localBundle.getString("logoUrl")

        if(logoUrl != "placeholder" && !logoUrl.isNullOrBlank()){
            Picasso.get().load(logoUrl).into(logo)
        }

        view.findViewById<EditText>(R.id.history_detail_company_name_text).
            setText(localBundle.getString("companyName"))
        view.findViewById<EditText>(R.id.history_detail_job_title_text).
            setText(localBundle.getString("jobTitle"))
        view.findViewById<EditText>(R.id.history_detail_job_location_text).
            setText(localBundle.getString("jobLocation"))
        view.findViewById<EditText>(R.id.history_detail_link_text).
            setText(localBundle.getString("link"))

        val jobStatus = localBundle.getString("status")
        updateDisplayedDate(jobStatus, view)


//        val rawDate = localBundle.getString("savedDate")
//        if(rawDate != null) {
//            val currentFormat = SimpleDateFormat("EEE LLL dd HH:mm:ss zzz yyyy")
//            val targetFormat = SimpleDateFormat("MMM d, yyyy")
//            val date = currentFormat.parse(rawDate)
//
//            view.findViewById<TextView>(R.id.history_detail_job_date_text).text =
//                targetFormat.format(date)
//        }


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
                        "${monthMap.entries.find { it.value == adjustedMonth }?.key} $day, $year"
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