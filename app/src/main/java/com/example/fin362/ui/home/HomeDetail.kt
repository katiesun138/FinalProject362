package com.example.fin362.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.fin362.R

class HomeDetail(jobBundle: Bundle) : Fragment() {
    val localBundle = jobBundle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_detail, container, false)

        view.findViewById<TextView>(R.id.history_detail_company_name_text).text =
            localBundle.getString("companyName")
        view.findViewById<TextView>(R.id.history_detail_job_title_text).text =
            localBundle.getString("jobTitle")
        view.findViewById<TextView>(R.id.history_detail_job_location_text).text =
            localBundle.getString("jobLocation")
        view.findViewById<TextView>(R.id.history_detail_job_date_text).text =
            localBundle.getString("jobDate")
        view.findViewById<TextView>(R.id.history_detail_link_text).text =
            localBundle.getString("link")

        return view
    }
}