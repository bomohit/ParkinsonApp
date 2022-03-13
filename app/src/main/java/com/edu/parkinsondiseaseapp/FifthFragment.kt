package com.edu.parkinsondiseaseapp

import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log.d
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.ViewModelProvider
import com.edu.parkinsondiseaseapp.databinding.FragmentFifthBinding
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

        return binding.root
    }

   override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStart.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isRecording -> stopRecorder()
                else -> startRecording()
            }
        }

       binding.btnSave.setOnClickListener {
           saveData()
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
        initiateLineChart()
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
        btn(true)
        d("bomoh", "timer:startRecording")
    }

    private fun stopRecorder() {
        timer.stop()
        btn(false)
    }

    override fun onTimerTick(duration: Long) {
        // timer
        val amplitude = recorder.maxAmplitude.toFloat()
        d("bomoh", "timer: $duration || ${(duration.toDouble() / 1000) % 60} || $amplitude")
        GlobalScope.launch {
            updateGraph(duration,amplitude)
        }
    }

    private fun updateGraph(duration: Long, amplitude: Float) {
        sensorViewModel.addAmplitude(amplitude, ((duration.toDouble() / 1000) % 60).toFloat())
        axisXdataSet.notifyDataSetChanged()

        binding.LineChart.notifyDataSetChanged()
        binding.LineChart.data.notifyDataChanged()

        binding.LineChart.invalidate()

        val data = binding.LineChart.data
//
//        // Move to the graph to the latest data
        binding.LineChart.setVisibleXRangeMaximum(10F)
        binding.LineChart.moveViewToX(data.entryCount.toFloat())
        d("bomoh", "data: ${sensorViewModel.axisX}")
    }

    private fun btn(btnStatus: Boolean) {
        when(btnStatus) {
            true -> {
                binding.btnStart.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red)
                binding.btnStart.text = getString(R.string.btnStop)
                binding.btnSave.isEnabled = false
            }
            false -> {
                binding.btnStart.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.teal_200)
                binding.btnStart.text = getString(R.string.btnStart)
                binding.btnSave.isEnabled = true
            }
        }
    }

    private fun disableBtn(state: Boolean) {
        // disable button
        requireActivity().findViewById<ImageButton>(R.id.backButton).isEnabled = !state
        binding.btnSave.isEnabled = !state
        binding.btnSave.isEnabled = !state
        binding.progressBar3.isInvisible = !state
    }

    private fun saveData() {
        if (!mainViewModel.permissionExternalDirGranted) {
            ActivityCompat.requestPermissions( requireActivity(), mainViewModel.permissionDir, mainViewModel.REQUEST_CODE_DIR)
            return
        }
        disableBtn(true)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val filename = "parkinsonApp_voice_${formatter.format(now)}.csv"
        val content = sensorViewModel.convertAmplitudeToCSV()
        try {
            val writer = FileOutputStream(File(path, filename))
            writer.write(content.toByteArray())
            writer.close()
            Toast.makeText(context, "File Saved in Download Folder", Toast.LENGTH_SHORT).show()
            disableBtn(false)
            d("bomoh", "FilePath: $path")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "WriteError: $e", Toast.LENGTH_SHORT).show()
            d("bomoh", "FilePath: $e")
        }
    }

    private fun initiateLineChart() {
        sensorViewModel.clear()
        sensorViewModel.addAmplitude(0F, 0F)

        axisXdataSet = LineDataSet(sensorViewModel.axisX, sensorViewModel.amplitudeLabel)
        sensorViewModel.xDataCollection.add(axisXdataSet)
        axisXData = LineData(sensorViewModel.xDataCollection as List<ILineDataSet>?)
        Collections.sort(sensorViewModel.axisX, EntryXComparator())

        axisXdataSet.axisDependency = YAxis.AxisDependency.LEFT
        axisXdataSet.setDrawValues(false)

        binding.LineChart.data = axisXData
        binding.LineChart.setScaleEnabled(true)
        binding.LineChart.isDragEnabled = true
        binding.LineChart.setVisibleXRangeMaximum(100F)
        binding.LineChart.description.isEnabled = false

        axisXdataSet.color = Color.RED
        axisXdataSet.valueTextSize = 13f

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().findViewById<ImageButton>(R.id.backButton).isInvisible = false
    }
}