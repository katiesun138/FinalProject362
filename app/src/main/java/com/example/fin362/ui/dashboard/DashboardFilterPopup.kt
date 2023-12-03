package com.example.fin362.ui.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.fin362.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardFilterPopup : DialogFragment() {

    interface FilterPopupListener {
        suspend fun onFiltersApplied(jobType: String, location: String, category: String)
    }

    var overlayView: View? = null
    private lateinit var spinnerJobType: Spinner
    private lateinit var spinnerLocation: Spinner
    private lateinit var spinnerCategory: Spinner

    var filterPopupListener: FilterPopupListener? = null

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is FilterPopupListener) {
//            filterPopupListener = context
//        } else {
//            throw ClassCastException("$context must implement FilterPopupListener")
//        }
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_dashboard_filter_popup, container, false)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.layoutParams = layoutParams


        val doneButton= view.findViewById<Button>(R.id.doneButton)
        val closeButton = view.findViewById<Button>(R.id.closeButton)
        spinnerJobType = view.findViewById(R.id.spinnerJobType)
        spinnerLocation = view.findViewById(R.id.spinnerLocation)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
//

        doneButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//
                applyFilters()
            }

            dialog?.dismiss()

        }

        closeButton.setOnClickListener {
            dialog?.dismiss()  // Close the dialog
        }

        return view
    }
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is FilterPopupListener) {
//            filterPopupListener = context
//        } else {
//            throw ClassCastException("$context must implement FilterPopupListener")
//        }
//    }


    private suspend fun applyFilters() {
        // Get selected values from spinners
        val selectedJobType = spinnerJobType.selectedItem.toString()
        val selectedLocation = spinnerLocation.selectedItem.toString()
        val selectedCategory = spinnerCategory.selectedItem.toString()

        // Notify the listener with the selected filters
        filterPopupListener?.onFiltersApplied(selectedJobType, selectedLocation, selectedCategory)

        // Dismiss the fragment
        dismiss()
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view?.findViewById<View>(R.id.overlayView)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        overlayView?.visibility = View.GONE
        super.onDestroyView()
    }


}