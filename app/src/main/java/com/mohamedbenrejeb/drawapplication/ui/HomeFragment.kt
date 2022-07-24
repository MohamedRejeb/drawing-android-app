package com.mohamedbenrejeb.drawapplication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mohamedbenrejeb.drawapplication.databinding.FragmentHomeBinding
import com.mohamedbenrejeb.drawapplication.models.DrawingType

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newDrawingBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFirstFragment())
        }
        binding.colorNumbersBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseDrawingFragment(
                DrawingType.Number
            ))
        }
        binding.colorLettersBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseDrawingFragment(
                DrawingType.Letter
            ))        }
        binding.colorAnimalsBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChooseDrawingFragment(
                DrawingType.Animal
            ))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}