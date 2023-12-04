package com.example.fin362.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    var jobsResult: List<DashboardFragment.Result>? = null
    var otherJobsResult: List<DashboardFragment.JobResult>? = null
    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
}