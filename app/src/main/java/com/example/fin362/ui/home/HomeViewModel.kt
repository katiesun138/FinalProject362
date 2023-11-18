package com.example.fin362.ui.home

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var compactView: Boolean = false
}

class HomeGraphViewModel: ViewModel() {
    var graphType: Int = 0
}