package com.example.fin362.ui.home

import androidx.lifecycle.ViewModel
import com.example.fin362.FirebaseDBManager
import com.example.fin362.ui.events.Job

open class HomeViewModel : ViewModel() {
    var compactView: Boolean = false
    var db = FirebaseDBManager()
    var jobs: List<Job> = listOf()
}

class HomeGraphViewModel: HomeViewModel() {
    var graphType: Int = 0
}