package com.example.fin362.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.fin362.R
import com.example.fin362.databinding.FragmentDashboardBinding


class DashboardDetailed : Fragment() {
    private var _binding: FragmentDashboardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_dashboard_detailed, container, false)

        var backBtn = view.findViewById<Button>(R.id.backToDashboard)

        return inflater.inflate(R.layout.fragment_dashboard_detailed, container, false)

    }

}