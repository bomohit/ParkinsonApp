package com.edu.parkinsondiseaseapp

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val REQUEST_CODE = 200
    var permission = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    var permissionGranted = false
}