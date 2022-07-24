package com.mohamedbenrejeb.drawapplication.ui

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mohamedbenrejeb.drawapplication.R
import com.mohamedbenrejeb.drawapplication.adapters.SavedDrawingAdapter
import com.mohamedbenrejeb.drawapplication.databinding.FragmentSavedDrawingsBinding
import com.mohamedbenrejeb.drawapplication.models.SavedDrawing
import com.mohamedbenrejeb.drawapplication.utils.drawingsDir
import java.io.File

class SavedDrawingsFragment : Fragment() {
    private var _binding: FragmentSavedDrawingsBinding? = null
    private val binding get() = _binding!!

    private var openedDrawingFileAbsolutePath: String? = null
    private var isDeletionActionBarActive: Boolean = false

    private val selectedSavedDrawingsToDelete = ArrayList<SavedDrawing>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedDrawingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedDrawingsDir = File(drawingsDir)

        val savedDrawings = savedDrawingsDir.listFiles()?.reversed()?.map { file ->
            SavedDrawing(file = file)
        }
        val savedDrawingAdapter = SavedDrawingAdapter(
            { savedDrawing ->
                if (isDeletionActionBarActive) {
                    selectSavedDrawingToDelete(savedDrawing)
                } else {
                    openedDrawingFileAbsolutePath = savedDrawing.file.absolutePath
                    setDrawingBitmap(file = savedDrawing.file)
                    openDrawingOverlay()
                }
            },
            { savedDrawing ->
                enableDeletionAppBar()
                selectSavedDrawingToDelete(savedDrawing)
            }
        )

        binding.savedDrawingsRv.let {
            it.setHasFixedSize(true)
            it.adapter = savedDrawingAdapter
        }

        savedDrawingAdapter.submitList(savedDrawings)

        binding.noSavedDrawingsTv.visibility =
            if (savedDrawings.isNullOrEmpty()) View.VISIBLE
            else View.GONE

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.overlayView.setOnClickListener {
            closeDrawingOverlay()
        }

        binding.drawingCv.setOnClickListener {  }

        binding.cancelBtn.setOnClickListener {
            selectedSavedDrawingsToDelete.clear()
            disableDeletionAppBar()
            disableDeletionRadio()
        }

        binding.deleteBtn.setOnClickListener {
            deleteSelectedDrawings()
        }

        binding.editBtn.setOnClickListener {
            openedDrawingFileAbsolutePath?.let { path ->
                val fileName = File(path).name.split(".").first()
                val words = fileName.split("-")
                val drawingId = words.last().toInt()

                findNavController().navigate(
                    SavedDrawingsFragmentDirections.actionSavedDrawingsFragmentToDrawingFragment(
                        drawingId = drawingId,
                        filePath = path
                    )
                )
            }
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do custom work here
                    if (isDeletionActionBarActive) {
                        isEnabled = false
                        disableDeletionAppBar()
                        disableDeletionRadio()
                    }
                    // if you want onBackPressed() to be called as normal afterwards
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }

    private fun setDrawingBitmap(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        binding.drawingIv.setImageBitmap(bitmap)
    }

    private fun openDrawingOverlay() {
        binding.overlayView.alpha = 0f
        binding.overlayView.visibility = View.VISIBLE

        binding.overlayView
            .animate()
            .alpha(0.4f)
            .setDuration(ANIMATION_DURATION)
            .start()

        binding.drawingCv.alpha = 0f
        binding.drawingCv.visibility = View.VISIBLE

        binding.drawingCv
            .animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()
    }

    private fun closeDrawingOverlay() {
        openedDrawingFileAbsolutePath = null

        binding.overlayView.visibility = View.GONE

        binding.drawingCv
            .animate()
            .scaleX(ANIMATION_SCALE)
            .scaleY(ANIMATION_SCALE)
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.drawingCv.visibility = View.GONE
        }, ANIMATION_DURATION)
    }

    private fun enableDeletionAppBar() {
        if (isDeletionActionBarActive)
            return

        isDeletionActionBarActive = true

        binding.actionBar.setBackgroundColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(R.color.red_400, null)
            else
                resources.getColor(R.color.red_400)
        )

        binding.cancelBtn.alpha = 0f
        binding.cancelBtn.visibility = View.VISIBLE

        binding.cancelBtn
            .animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()

        binding.backBtn
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.backBtn.visibility = View.GONE
        }, ANIMATION_DURATION)

        binding.appBarTitle.text = "1 Drawing Selected"

        binding.deleteBtn.alpha = 0f
        binding.deleteBtn.visibility = View.VISIBLE

        binding.deleteBtn
            .animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()

        binding.searchBtn
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.searchBtn.visibility = View.GONE
        }, ANIMATION_DURATION)
    }

    private fun disableDeletionAppBar() {
        if (!isDeletionActionBarActive)
            return

        isDeletionActionBarActive = false

        binding.actionBar.setBackgroundColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(R.color.white, null)
            else
                resources.getColor(R.color.white)
        )

        binding.backBtn.alpha = 0f
        binding.backBtn.visibility = View.VISIBLE

        binding.backBtn
            .animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()

        binding.cancelBtn
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.cancelBtn.visibility = View.GONE
        }, ANIMATION_DURATION)

        binding.appBarTitle.text = "Saved Drawings"

        binding.searchBtn.alpha = 0f
        binding.searchBtn.visibility = View.VISIBLE

        binding.searchBtn
            .animate()
            .alpha(1f)
            .setDuration(ANIMATION_DURATION)
            .start()

        binding.deleteBtn
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            binding.deleteBtn.visibility = View.GONE
        }, ANIMATION_DURATION)
    }

    private fun disableDeletionRadio(list: ArrayList<SavedDrawing>? = null) {
        val savedDrawings =
            list ?: ArrayList((binding.savedDrawingsRv.adapter as SavedDrawingAdapter).currentList)

        savedDrawings.forEachIndexed { index, savedDrawing ->
            savedDrawings[index] = savedDrawing.copy(
                isSelected = false,
                isSelectionActive = false
            )
        }
        (binding.savedDrawingsRv.adapter as SavedDrawingAdapter).submitList(savedDrawings)

        binding.noSavedDrawingsTv.visibility =
            if (savedDrawings.isEmpty()) View.VISIBLE
            else View.GONE
    }

    private fun selectSavedDrawingToDelete(savedDrawing: SavedDrawing) {
        if (selectedSavedDrawingsToDelete.none { it.id == savedDrawing.id }) {
            selectedSavedDrawingsToDelete.add(savedDrawing)
        } else {
            val savedDrawingIndex = selectedSavedDrawingsToDelete.indexOfFirst { it.id == savedDrawing.id }
            selectedSavedDrawingsToDelete.removeAt(savedDrawingIndex)
        }

        binding.appBarTitle.text = "${selectedSavedDrawingsToDelete.size} Drawing Selected"

        val savedDrawings = ArrayList((binding.savedDrawingsRv.adapter as SavedDrawingAdapter).currentList)
        savedDrawings.forEachIndexed { index, _ ->
            savedDrawings[index] = savedDrawings[index].copy(isSelectionActive = true)
        }
        val savedDrawingIndex = savedDrawings.indexOfFirst { it.id == savedDrawing.id }
        savedDrawings[savedDrawingIndex] =
            savedDrawings[savedDrawingIndex].copy(isSelected = !savedDrawings[savedDrawingIndex].isSelected)
        (binding.savedDrawingsRv.adapter as SavedDrawingAdapter).submitList(savedDrawings)
    }

    private fun deleteSelectedDrawings() {
        val savedDrawings = ArrayList((binding.savedDrawingsRv.adapter as SavedDrawingAdapter).currentList)

        selectedSavedDrawingsToDelete.forEach { savedDrawing ->
            if (savedDrawing.file.delete()) {
                val drawingIndex = savedDrawings.indexOfFirst { it.id == savedDrawing.id }
                savedDrawings.removeAt(drawingIndex)
            }
        }

        selectedSavedDrawingsToDelete.clear()

        disableDeletionAppBar()
        disableDeletionRadio(savedDrawings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ANIMATION_SCALE = 0.2f
        private const val ANIMATION_DURATION = 200L
    }
}