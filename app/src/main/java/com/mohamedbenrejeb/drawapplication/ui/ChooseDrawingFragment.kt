package com.mohamedbenrejeb.drawapplication.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mohamedbenrejeb.drawapplication.R
import com.mohamedbenrejeb.drawapplication.adapters.DrawingAdapter
import com.mohamedbenrejeb.drawapplication.data.DrawingData
import com.mohamedbenrejeb.drawapplication.databinding.FragmentChooseDrawingBinding
import com.mohamedbenrejeb.drawapplication.models.DrawingType

class ChooseDrawingFragment : Fragment() {
    private var _binding: FragmentChooseDrawingBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ChooseDrawingFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChooseDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.savedDrawingsFab.setOnClickListener {
            findNavController().navigate(
                ChooseDrawingFragmentDirections.actionChooseDrawingFragmentToSavedDrawingsFragment()
            )
        }

        binding.appBarTitle.text =
            when (args.drawingType) {
                DrawingType.Number -> "Choose Number"
                DrawingType.Letter -> "Choose Letter"
                DrawingType.Animal -> "Choose Animal"
            }

        val drawingList = DrawingData.getDrawingListByType(args.drawingType)
        val adapter = DrawingAdapter(drawingList) { drawing ->
            findNavController().navigate(
                ChooseDrawingFragmentDirections.actionChooseDrawingFragmentToDrawingFragment(
                    drawingId = drawing.id
                )
            )
        }
        binding.drawingsRv.let {
            it.setHasFixedSize(true)
            it.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}