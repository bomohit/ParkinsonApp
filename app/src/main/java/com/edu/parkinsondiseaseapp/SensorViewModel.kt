package com.edu.parkinsondiseaseapp

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlin.collections.ArrayList

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

    var axisX: ArrayList<Entry> = ArrayList()
    var axisY: ArrayList<Entry> = ArrayList()
    var axisZ: ArrayList<Entry> = ArrayList()
    var xDataCollection: ArrayList<LineDataSet> = ArrayList()
    var yDataCollection: ArrayList<LineDataSet> = ArrayList()
    var zDataCollection: ArrayList<LineDataSet> = ArrayList()
    var xyzDataCollection: ArrayList<LineDataSet> = ArrayList()

    val gyroLabel: String = "gyro"
    val xLabel: String = "X"
    val yLabel: String = "Y"
    val zLabel: String = "Z"

    var currentTime : Long = 0
    var lastCheck : Long = 0

    fun gyro(x: Float, y: Float, z: Float, time: Int) {
        axisX.add(Entry(time.toFloat(),x))
        axisY.add(Entry(time.toFloat(),y))
        axisZ.add(Entry(time.toFloat(),z))
    }

    fun clear() {
        axisX.clear()
        axisY.clear()
        axisZ.clear()
        xDataCollection.clear()
    }
}