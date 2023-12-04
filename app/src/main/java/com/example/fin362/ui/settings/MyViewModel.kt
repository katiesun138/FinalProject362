package com.example.fin362.ui.settings

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
class MyViewModel:ViewModel() {
    val userImage = MutableLiveData<Bitmap>()
}