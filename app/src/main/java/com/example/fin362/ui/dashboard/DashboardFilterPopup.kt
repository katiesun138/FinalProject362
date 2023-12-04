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

    object SharedPreferencesHelper {

        private const val PREF_NAME = "FilterPrefs"
        private const val KEY_JOB_TYPE = "jobType"
        private const val KEY_LOCATION = "location"
        private const val KEY_CATEGORY = "category"

        fun saveFilterValues(context: Context, jobType: String, location: String, category: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString(KEY_JOB_TYPE, jobType)
            editor.putString(KEY_LOCATION, location)
            editor.putString(KEY_CATEGORY, category)
            editor.apply()
        }

        fun getFilterValues(context: Context): Triple<String, String, String> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return Triple(
                prefs.getString(KEY_JOB_TYPE, "") ?: "",
                prefs.getString(KEY_LOCATION, "") ?: "",
                prefs.getString(KEY_CATEGORY, "") ?: ""
            )
        }

        fun clearFilterValues(context: Context) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.clear()
            editor.apply()
        }

    }

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


        val doneButton = view.findViewById<Button>(R.id.doneButton)
        val closeButton = view.findViewById<Button>(R.id.closeButton)
        spinnerJobType = view.findViewById(R.id.spinnerJobType)
        spinnerLocation = view.findViewById(R.id.spinnerLocation)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)

        val (savedJobType, savedLocation, savedCategory) = context?.let {
            SharedPreferencesHelper.getFilterValues(it)
        } ?: Triple("", "", "")

        val locations = resources.getStringArray(R.array.locations)
        val jobTypes = resources.getStringArray(R.array.job_types)
        val categories = resources.getStringArray(R.array.categories)

        setSpinnerSelection(spinnerJobType, savedJobType, jobTypes)
        setSpinnerSelection(spinnerLocation, savedLocation, locations)
        setSpinnerSelection(spinnerCategory, savedCategory, categories)

        doneButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                applyFilters()
            }
            dialog?.dismiss()
        }

        closeButton.setOnClickListener {
            dialog?.dismiss()
        }

        return view
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String, data: Array<String>) {
        val position = data.indexOf(value)
        if (position != -1) {
            spinner.setSelection(position)
        }
    }

    private suspend fun applyFilters() {
        // Get selected values from spinners
        val selectedJobType = spinnerJobType.selectedItem.toString()
        val selectedLocation = spinnerLocation.selectedItem.toString()
        val selectedCategory = spinnerCategory.selectedItem.toString()
        context?.let {
            SharedPreferencesHelper.saveFilterValues(it, selectedJobType, selectedLocation, selectedCategory)
        }


        filterPopupListener?.onFiltersApplied(selectedJobType, selectedLocation, selectedCategory)
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

    //once view gets destroyed
    override fun onDestroyView() {
        overlayView?.visibility = View.GONE
        super.onDestroyView()
    }

    //once the application gets destroyed
//    override fun onDestroy() {
//        clearSharedPreferences()
//        super.onDestroy()
//    }

    private fun clearSharedPreferences() {
        context?.let {
            SharedPreferencesHelper.clearFilterValues(it)
        }
    }


}