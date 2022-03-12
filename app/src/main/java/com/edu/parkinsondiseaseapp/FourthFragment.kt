package com.edu.parkinsondiseaseapp

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Half
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.FragmentFourthBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FourthFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentFourthBinding? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor

    private val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4) { 0f }
    private var timestamp: Float = 0f

    private lateinit var axisXdataSet: LineDataSet
    private lateinit var axisYdataSet: LineDataSet
    private lateinit var axisZdataSet: LineDataSet
    private lateinit var axisXYZaxisData: LineData

    lateinit var sensorViewModel: SensorViewModel
    private var count = 0

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFourthBinding.inflate(inflater, container, false)
        sensorViewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        axisXdataSet = LineDataSet(sensorViewModel.axisX, sensorViewModel.xLabel)
        axisYdataSet = LineDataSet(sensorViewModel.axisY, sensorViewModel.yLabel)
        axisZdataSet = LineDataSet(sensorViewModel.axisZ, sensorViewModel.zLabel)

        sensorViewModel.xyzDataCollection.add(axisXdataSet)
        sensorViewModel.xyzDataCollection.add(axisYdataSet)
        sensorViewModel.xyzDataCollection.add(axisZdataSet)

        axisXYZaxisData = LineData(sensorViewModel.xyzDataCollection as List<ILineDataSet>?)
        Collections.sort(sensorViewModel.axisX, EntryXComparator())
        Collections.sort(sensorViewModel.axisY, EntryXComparator())
        Collections.sort(sensorViewModel.axisZ, EntryXComparator())

        axisXdataSet.axisDependency = YAxis.AxisDependency.LEFT
        axisYdataSet.axisDependency = YAxis.AxisDependency.LEFT
        axisZdataSet.axisDependency = YAxis.AxisDependency.LEFT

        axisXdataSet.setDrawValues(false)
        axisYdataSet.setDrawValues(false)
        axisZdataSet.setDrawValues(false)

        binding.LineChart.data = axisXYZaxisData
        binding.LineChart.setScaleEnabled(true)
        binding.LineChart.isDragEnabled = true

        axisXdataSet.color = Color.RED
        axisYdataSet.color = Color.GREEN
        axisZdataSet.color = Color.BLUE

        axisXdataSet.valueTextSize = 13f
        axisYdataSet.valueTextSize = 13f
        axisZdataSet.valueTextSize = 13f

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var a = false
        binding.fourthButton.setOnClickListener {
            if (!a) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                sensorViewModel.lastCheck = System.currentTimeMillis()
                a = true
            } else {
                sensorManager.unregisterListener(this)
                a = false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("onSensorChanged", "$event")
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0f && event != null) {
            val dT = (event.timestamp - timestamp) * NS2S
            // Axis of the rotation sample, not normalized yet.
            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]
            Log.d("bomoh", "x1 = ${String.format("%.2f", axisX)}, timestamp = $timestamp")
            sensorViewModel.currentTime = System.currentTimeMillis()
            when {
                (sensorViewModel.currentTime - sensorViewModel.lastCheck) >= 1000 -> {
                    Log.d(
                        "bomoh",
                        "tme: ${sensorViewModel.currentTime - sensorViewModel.lastCheck} :: cT: ${sensorViewModel.currentTime} :: lC: ${sensorViewModel.lastCheck}"
                    )
                    sensorViewModel.gyro(axisX,axisY,axisZ, count)
                    updateGraph()
                    count+=1
                    sensorViewModel.lastCheck = System.currentTimeMillis()
                }
            }



            // Calculate the angular speed of the sample
            val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > Half.EPSILON) {
                axisX /= omegaMagnitude
                axisY /= omegaMagnitude
                axisZ /= omegaMagnitude
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            val thetaOverTwo: Float = omegaMagnitude * dT / 2.0f
            val sinThetaOverTwo: Float = sin(thetaOverTwo)
            val cosThetaOverTwo: Float = cos(thetaOverTwo)
            deltaRotationVector[0] = sinThetaOverTwo * axisX
            deltaRotationVector[1] = sinThetaOverTwo * axisY
            deltaRotationVector[2] = sinThetaOverTwo * axisZ
            deltaRotationVector[3] = cosThetaOverTwo
        }
        timestamp = event?.timestamp?.toFloat() ?: 0f
        val deltaRotationMatrix = FloatArray(9) { 0f }
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun updateGraph() {
        axisXdataSet.notifyDataSetChanged()
        axisYdataSet.notifyDataSetChanged()
        axisZdataSet.notifyDataSetChanged()
        axisXYZaxisData.notifyDataChanged()

        binding.LineChart.notifyDataSetChanged()
        binding.LineChart.data.notifyDataChanged()

        binding.LineChart.invalidate()

        val data = binding.LineChart.data

        // Move to the graph to the latest data
        binding.LineChart.setVisibleXRangeMaximum(10F)
        binding.LineChart.moveViewToX(data.entryCount.toFloat())
        binding.LineChart.setVisibleXRangeMaximum(10F)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}