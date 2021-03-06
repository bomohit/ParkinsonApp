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
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.FragmentSecondBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentSecondBinding? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var viewModel : SensorViewModel
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4) { 0f }
    private var timestamp: Float = 0f
    private lateinit var gyroXdataSet: LineDataSet
    private lateinit var gyroYdataSet: LineDataSet
    private lateinit var gyroZdataSet: LineDataSet
    private lateinit var gyroXaxisData: LineData
    private lateinit var gyroYaxisData: LineData
    private lateinit var gyroZaxisData: LineData
    lateinit var sensorViewModel: SensorViewModel
    private var count = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        sensorViewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        gyroXdataSet = LineDataSet(sensorViewModel.axisX, sensorViewModel.gyroLabel)
        gyroYdataSet = LineDataSet(sensorViewModel.axisY, sensorViewModel.gyroLabel)
        gyroZdataSet = LineDataSet(sensorViewModel.axisZ, sensorViewModel.gyroLabel)

        sensorViewModel.xDataCollection.add(gyroXdataSet)
        sensorViewModel.yDataCollection.add(gyroYdataSet)
        sensorViewModel.zDataCollection.add(gyroZdataSet)

        gyroXaxisData = LineData(sensorViewModel.xDataCollection as List<ILineDataSet>?)
        gyroYaxisData = LineData(sensorViewModel.yDataCollection as List<ILineDataSet>?)
        gyroZaxisData = LineData(sensorViewModel.zDataCollection as List<ILineDataSet>?)

        Collections.sort(sensorViewModel.axisX, EntryXComparator())
        Collections.sort(sensorViewModel.axisY, EntryXComparator())
        Collections.sort(sensorViewModel.axisZ, EntryXComparator())

//        gyroDataSet.lineWidth = 3f
        gyroXdataSet.axisDependency = YAxis.AxisDependency.LEFT
        gyroYdataSet.axisDependency = YAxis.AxisDependency.LEFT
        gyroZdataSet.axisDependency = YAxis.AxisDependency.LEFT

        gyroXdataSet.setDrawValues(false)
        gyroYdataSet.setDrawValues(false)
        gyroZdataSet.setDrawValues(false)

        binding.XChart.data = gyroXaxisData
        binding.YChart.data = gyroYaxisData
        binding.ZChart.data = gyroZaxisData

        binding.XChart.setScaleEnabled(true)
        binding.YChart.setScaleEnabled(true)
        binding.ZChart.setScaleEnabled(true)

        binding.XChart.isDragEnabled = true
        binding.YChart.isDragEnabled = true
        binding.ZChart.isDragEnabled = true

        gyroXdataSet.color = Color.BLACK
        gyroYdataSet.color = Color.BLACK
        gyroZdataSet.color = Color.BLACK
//        lineDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
        gyroXdataSet.valueTextColor= Color.BLUE
        gyroYdataSet.valueTextColor= Color.BLUE
        gyroZdataSet.valueTextColor= Color.BLUE

        gyroXdataSet.valueTextSize = 13f
        gyroYdataSet.valueTextSize = 13f
        gyroZdataSet.valueTextSize = 13f

        gyroXdataSet.setDrawFilled(true)
        gyroYdataSet.setDrawFilled(true)
        gyroZdataSet.setDrawFilled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var a = false
        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            if (!a) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
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
                    d("bomoh", "tme: ${sensorViewModel.currentTime - sensorViewModel.lastCheck} :: cT: ${sensorViewModel.currentTime} :: lC: ${sensorViewModel.lastCheck}")
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

        gyroXdataSet.notifyDataSetChanged()
        gyroXaxisData.notifyDataChanged()
        gyroYdataSet.notifyDataSetChanged()
        gyroYaxisData.notifyDataChanged()
        gyroZdataSet.notifyDataSetChanged()
        gyroZaxisData.notifyDataChanged()
//        Collections.sort(sensorViewModel.gyroX, EntryXComparator())
//        Collections.sort(sensorViewModel.gyroY, EntryXComparator())
//        Collections.sort(sensorViewModel.gyroZ, EntryXComparator())

        binding.XChart.notifyDataSetChanged()
        binding.XChart.data.notifyDataChanged()
        binding.YChart.notifyDataSetChanged()
        binding.YChart.data.notifyDataChanged()
        binding.ZChart.notifyDataSetChanged()
        binding.ZChart.data.notifyDataChanged()

        binding.XChart.invalidate()
        binding.YChart.invalidate()
        binding.ZChart.invalidate()

        val dataX = binding.XChart.data
        val dataY = binding.YChart.data
        val dataZ = binding.ZChart.data

        // Move to the graph to the latest data
        binding.XChart.setVisibleXRangeMaximum(10F)
        binding.XChart.moveViewToX(dataX.entryCount.toFloat())
        binding.YChart.setVisibleXRangeMaximum(10F)
        binding.YChart.moveViewToX(dataY.entryCount.toFloat())
        binding.ZChart.setVisibleXRangeMaximum(10F)
        binding.ZChart.moveViewToX(dataZ.entryCount.toFloat())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}