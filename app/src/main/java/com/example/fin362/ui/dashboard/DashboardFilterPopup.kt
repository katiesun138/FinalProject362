package com.example.fin362.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.fin362.R

class DashboardFilterPopup : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_dashboard_filter_popup, container, false)
        val doneButton= view.findViewById<Button>(R.id.doneButton)
        val closeButton = view.findViewById<Button>(R.id.closeButton)

        doneButton.setOnClickListener {
            dialog?.dismiss()

        }

        closeButton.setOnClickListener {
            dialog?.dismiss()  // Close the dialog
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}