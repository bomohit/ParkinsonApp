package com.edu.parkinsondiseaseapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.edu.parkinsondiseaseapp.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectGyro.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ThirdFragment)
        }
        binding.btnSelectAccelerometer.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_FourthFragment)
        }
        binding.btnSelectVoice.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_FifthFragment)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().findViewById<ImageButton>(R.id.backButton).isInvisible = true
    }
}