package com.edu.parkinsondiseaseapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log.d
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry

class SensorViewModel : ViewModel() {
    private lateinit var sensorManager: SensorManager

    /*
    fun senss(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val deviceSensor: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        d("BomohDeviceSensor", "$deviceSensor")
        d("BomohDeviceSensorAccelerometer", "$aSensor")
    } */

    var lineList: ArrayList<Entry> = ArrayList()
    var lineList2: ArrayList<Entry> = ArrayList()

}