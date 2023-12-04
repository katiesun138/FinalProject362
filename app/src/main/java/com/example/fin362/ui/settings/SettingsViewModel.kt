package com.example.fin362.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fin362.FirebaseDBManager

class SettingsViewModel : ViewModel() {
    var db = FirebaseDBManager()
    var status:MutableList<String> = mutableListOf()
    private val _job = MutableLiveData<String>().apply {
        value = "Software Engineer"
    }
    val job: LiveData<String> = _job
    private val _company = MutableLiveData<String>().apply {
        value = "Spotify"
    }
    val company: LiveData<String> = _company
    private val _major = MutableLiveData<String>().apply {
        value = "BSc Computer Science"
    }
    val major: LiveData<String> = _major
    private val _school = MutableLiveData<String>().apply {
        value = "Simon Fraser"
    }
    val school: LiveData<String> = _school
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

    private val _offer = MutableLiveData<String>().apply {
        value = "14"
    }
    var offer: LiveData<String> = _offer

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