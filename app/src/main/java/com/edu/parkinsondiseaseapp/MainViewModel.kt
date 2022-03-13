package com.edu.parkinsondiseaseapp

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val REQUEST_CODE = 200
    val REQUEST_CODE_DIR = 201
    var permission = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    var permissionGranted = false

    var permissionExternalDirGranted = false
    val permissionDir = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
}