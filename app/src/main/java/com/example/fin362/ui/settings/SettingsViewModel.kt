package com.example.fin362.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val _username = MutableLiveData<String>().apply {
        value = "John Doe"
    }
    val username: LiveData<String> = _username

    private val _mail = MutableLiveData<String>().apply {
        value = "johndoe@gmail.com"
    }
    var mail: LiveData<String> = _mail

    private val _applied = MutableLiveData<String>().apply {
        value = "27"
    }
    var applied: LiveData<String> = _applied

    private val _interviewing = MutableLiveData<String>().apply {
        value = "19"
    }
    var interviewing: LiveData<String> = _interviewing

    private val _rejected = MutableLiveData<String>().apply {
        value = "14"
    }
    var rejected: LiveData<String> = _rejected

    private val _engineer = MutableLiveData<String>().apply {
        value = "San Jose,US"
    }
    var engineer: LiveData<String> = _engineer

    private val _spotify = MutableLiveData<String>().apply {
        value = "Dec 20 - Feb 21"
    }
    var spotify: LiveData<String> = _spotify

    private val _science = MutableLiveData<String>().apply {
        value = "Grad"
    }
    var science: LiveData<String> = _science

    private val _simon = MutableLiveData<String>().apply {
        value = "2024"
    }
    var simon: LiveData<String> = _simon

    private val _resume = MutableLiveData<String>().apply {
        value = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    }
    var resume: LiveData<String> = _resume
}