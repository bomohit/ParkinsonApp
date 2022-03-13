package com.edu.parkinsondiseaseapp

import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.FragmentFifthBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FifthFragment : Fragment(), Timer.OnTimerTickListener {

    private var _binding: FragmentFifthBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel : MainViewModel
    private lateinit var recorder: MediaRecorder
    private lateinit var axisXdataSet: LineDataSet
    private lateinit var axisXData: LineData
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false
    private var isPaused = false

    private lateinit var sensorViewModel: SensorViewModel
    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timer = Timer(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFifthBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        sensorViewModel = ViewModelProvider(this).get(SensorViewModel::class.java)
        sensorViewModel.addAmplitude(0F, 0F)

        axisXdataSet = LineDataSet(sensorViewModel.axisX, sensorViewModel.xLabel)
        sensorViewModel.xDataCollection.add(axisXdataSet)
        axisXData = LineData(sensorViewModel.xDataCollection as List<ILineDataSet>?)
        Collections.sort(sensorViewModel.axisX, EntryXComparator())

        axisXdataSet.axisDependency = YAxis.AxisDependency.LEFT
        axisXdataSet.setDrawValues(false)



        binding.LineChart.data = axisXData
        binding.LineChart.setScaleEnabled(true)
        binding.LineChart.isDragEnabled = true
        binding.LineChart.setVisibleXRangeMaximum(100F)

        axisXdataSet.color = Color.RED
        axisXdataSet.valueTextSize = 13f

        return binding.root
    }

   override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }
        }
    }

    private fun pauseRecorder() {
        recorder.pause()
        isPaused = true
        // Change btn img
        d("bomoh", "timer:pause")
        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPaused = false
        // change btn img
        d("bomoh", "timer:start")
        timer.start()
    }

    private fun startRecording() {
        if (!mainViewModel.permissionGranted) {
            ActivityCompat.requestPermissions( requireActivity(), mainViewModel.permission, mainViewModel.REQUEST_CODE)
            return
        }
        // start recording
        recorder = MediaRecorder()
        dirPath = "${requireActivity().externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss", Locale.getDefault())
        var date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            } catch (e: IOException) {}

            start()
        }

        // change btn image
        isRecording = true
        isPaused = false

        timer.start()
        d("bomoh", "timer:startRecording")
    }

    private fun stopRecorder() {
        timer.stop()
    }

    override fun onTimerTick(duration: Long) {
        // timer
        val applitude = recorder.maxAmplitude.toFloat()
        d("bomoh", "timer: $duration || ${(duration.toDouble() / 1000)} || $applitude")
        GlobalScope.launch {
            updateGraph(duration,applitude)
        }
    }

    private fun updateGraph(duration: Long, applitude: Float) {
        sensorViewModel.addAmplitude(applitude, (duration.toDouble() / 1000).toFloat())
        axisXdataSet.notifyDataSetChanged()

        binding.LineChart.notifyDataSetChanged()
        binding.LineChart.data.notifyDataChanged()

        binding.LineChart.invalidate()

        val data = binding.LineChart.data

        // Move to the graph to the latest data
//        binding.LineChart.setVisibleXRangeMaximum(10F)
        binding.LineChart.moveViewToX(data.entryCount.toFloat())
        d("bomoh", "data: ${sensorViewModel.axisX}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}