package com.edu.parkinsondiseaseapp

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Half.EPSILON
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.FragmentFirstBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var viewModel : SensorViewModel
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f
    private val deltaRotationVector = FloatArray(4) { 0f }
    private var timestamp: Float = 0f
    lateinit var linelist: ArrayList<Entry>
    lateinit var linelist2: ArrayList<Entry>
    lateinit var lineDataSetCollection: ArrayList<LineDataSet>
    lateinit var lineDataSet: LineDataSet
    lateinit var lineDataSet2: LineDataSet
    lateinit var lineData: LineData
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SensorViewModel::class.java)

        linelist = ArrayList()
        linelist.add(Entry(10f, 100f))
        linelist.add(Entry(20f, 300f))
        linelist.add(Entry(30f, 200f))
        linelist.add(Entry(40f, 600f))
        linelist.add(Entry(50f, 500f))

        linelist2 = ArrayList()
        linelist2.add(Entry(30f, 200f))
        linelist2.add(Entry(10f, 100f))
        linelist2.add(Entry(20f, 300f))
        linelist2.add(Entry(50f, 500f))
        linelist2.add(Entry(40f, 600f))

        lineDataSet = LineDataSet(linelist, "Data set 1")
        lineDataSet2 = LineDataSet(linelist2, "Data set 2")
        lineDataSetCollection = ArrayList()
        lineDataSetCollection.add(lineDataSet)
        lineDataSetCollection.add(lineDataSet2)

        lineData = LineData(lineDataSetCollection as List<ILineDataSet>?)

//        binding.LineChart.data = lineData
        binding.LineChart.data = lineData
        lineDataSet.color = Color.BLACK
//        lineDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
        lineDataSet.valueTextColor= Color.BLUE
        lineDataSet.valueTextSize = 13f
        lineDataSet.setDrawFilled(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var a = false

        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            /*
            if (!a) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
                a = true
            } else {
                sensorManager.unregisterListener(this)
                a = false
            }
            */
            linelist.add(Entry(80f, 600f))
            linelist2.add(Entry(70f, 700f))
            val data = binding.LineChart.data
//            linelist.removeAt(0)
            lineDataSet.notifyDataSetChanged()
            lineDataSet2.notifyDataSetChanged()
            binding.LineChart.data.notifyDataChanged()
            binding.LineChart.notifyDataSetChanged()
            binding.LineChart.invalidate()
            binding.LineChart.moveViewToX(data.entryCount.toFloat())
            d("bomoh", "lineList: $linelist")
            d("bomoh", "lineList2: $linelist2")
            d("bomoh", "lineData: $lineDataSetCollection")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        d("onSensorChanged", "$event")
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0f && event != null) {
            val dT = (event.timestamp - timestamp) * NS2S
            // Axis of the rotation sample, not normalized yet.
            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]
            d("bomoh", "x1 = ${String.format("%.2f", axisX)}, timestamp = $timestamp")
            // Calculate the angular speed of the sample
            val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}