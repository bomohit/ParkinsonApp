package com.edu.parkinsondiseaseapp

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlin.collections.ArrayList

class SensorViewModel : ViewModel() {
    private lateinit var sensorManager: SensorManager

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
    val amplitudeLabel: String = "Amplitude"

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
        yDataCollection.clear()
        zDataCollection.clear()
        xyzDataCollection.clear()
    }

    fun addAmplitude(x: Float, time: Float) {
        axisX.add(Entry(time, x))
    }

    fun convertToCSV(): String {
        var content = "time,X,Y,Z\n"
        axisX.forEachIndexed { i, _ ->
            content += "${axisX[i].x},${axisX[i].y},${axisY[i].y},${axisZ[i].y}\n"

        }

        return content
    }

    fun convertAmplitudeToCSV(): String {
        var content = "time,amplitude\n"
        axisX.forEachIndexed { i, _ ->
            content += "${axisX[i].x},${axisX[i].y}\n"

        }

        return content
    }
}