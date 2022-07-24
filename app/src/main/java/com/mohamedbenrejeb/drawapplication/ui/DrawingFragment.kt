package com.mohamedbenrejeb.drawapplication.ui

import android.annotation.SuppressLint
import android.graphics.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mohamedbenrejeb.drawapplication.R
import com.mohamedbenrejeb.drawapplication.data.DrawingData
import com.mohamedbenrejeb.drawapplication.databinding.FragmentDrawingBinding
import com.mohamedbenrejeb.drawapplication.models.Drawing
import com.mohamedbenrejeb.drawapplication.models.GoToType
import com.mohamedbenrejeb.drawapplication.models.Tool
import com.mohamedbenrejeb.drawapplication.utils.drawingsDir
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*


class DrawingFragment : Fragment() {

    private var _binding: FragmentDrawingBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<DrawingFragmentArgs>()

    private var selectedTool: Tool = Tool.Brush
    private var selectedColor: Int = R.color.red
    private var selectedBrushSize: Float = 20f
    private var selectedEraserSize: Float = 20f

    private val canvas = Canvas()
    private var bitmap: Bitmap? = null
    private val brushPaint = Paint().let {
        it.style = Paint.Style.FILL
        it.strokeJoin = Paint.Join.ROUND
        it.strokeWidth = selectedBrushSize * 2
        it.strokeCap = Paint.Cap.ROUND
        it
    }
    private val eraserPaint = Paint().let {
        it.style = Paint.Style.FILL
        it.strokeJoin = Paint.Join.ROUND
        it.strokeWidth = selectedEraserSize * 2
        it.strokeCap = Paint.Cap.ROUND
        it
    }
    private var lastBrushPoint: Point? = null
    private val bitmapList = ArrayList<Bitmap>()
    private var currentBitmapIndex: Int = 0

    private var drawing: Drawing? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isNewDrawing = args.drawingId == -1
        if (args.drawingId != -1) {
            drawing = DrawingData.getDrawingById(args.drawingId)
        }
        initDrawing(isNewDrawing)

        initOverlay()

        initUndoRedoButtons()

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        eraserPaint.color =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(R.color.white, null)
            else
                resources.getColor(R.color.white)

        initColorPicker()
        initToolButtons()
        initSlider()

        binding.canvasIv.setOnTouchListener { _, p1 ->
            if (bitmap == null) {
                clearBitmap()
            }

            when (selectedTool) {
                Tool.Brush -> {
                    drawWithBrushAction(event = p1)
                }
                Tool.Pail -> {
                    drawWithPailAction(event = p1)
                }
                Tool.Eraser -> {
                    eraseAction(event = p1)
                }
                Tool.Colors -> {
                    onSliderToolSelected(binding.brush, Tool.Brush)
                    drawWithBrushAction(event = p1)
                }
            }

            binding.canvasIv.setImageBitmap(bitmap)
            true
        }

        binding.saveBtn.setOnClickListener {
            onSaveAction()
        }

    }

    private fun initDrawing(isNewDrawing: Boolean) {
        val isEdit = !isNewDrawing && args.filePath != null

        if (isEdit) {
            clearBitmap()

            Handler(Looper.getMainLooper()).postDelayed({
                canvas.drawBitmap(
                    BitmapFactory.decodeFile(args.filePath),
                    Matrix(),
                    null
                )
                binding.canvasIv.setImageBitmap(bitmap)
            }, 100)
        }

        Log.d("isEdit", "$isEdit")

        binding.canvasIv.setImageBitmap(bitmap)

        binding.undo.isEnabled = false
        binding.redo.isEnabled = false

        if (isNewDrawing) {
            binding.appBarTitle.text = "New Drawing"
            binding.drawingIv.visibility = View.INVISIBLE
        } else if (isEdit && args.drawingId == 0) {
            binding.appBarTitle.text = "Edit Drawing"
            binding.drawingIv.visibility = View.INVISIBLE
        } else {
            binding.appBarTitle.text = drawing!!.text
            binding.drawingIv.setImageResource(drawing!!.imageResource)
            binding.drawingIv.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlay() {
        if (drawing == null || drawing?.tutorialPoints?.isEmpty() == true)
            return

        binding.skipTutorialTv.visibility = View.VISIBLE

        binding.skipTutorialTv.setOnClickListener {
            binding.hand.visibility = View.GONE
            binding.overlayIv.visibility = View.GONE
            binding.skipTutorialTv.visibility = View.GONE
        }

        binding.overlayIv.post {
            binding.overlayIv.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    Log.d("clickPoint", "x: ${motionEvent.x}, y: ${motionEvent.y}")
                    val drawAryaX = binding.drawingCv.x
                    val drawAryaY = binding.drawingCv.y
                    val drawAryaWidth = binding.drawingCv.width
                    val drawAryaHeight = binding.drawingCv.height

                    val clickX = motionEvent.x - drawAryaX
                    val clickY = motionEvent.y - drawAryaY


                    Log.d("clickPoint", "clickX: $clickX, clickY: $clickY")

                    val clickXPercent = (clickX * 100) / drawAryaWidth
                    val clickYPercent = (clickY * 100) / drawAryaHeight
                    Log.d("clickPoint", "xPercent: $clickXPercent, yPercent: $clickYPercent")
                }
                true
            }

            var overlayBitmap = Bitmap.createBitmap(
                binding.overlayIv.width,
                binding.overlayIv.height,
                Bitmap.Config.ARGB_8888
            )

            val overlayCanvas = Canvas(overlayBitmap)

            val overlayPaint = Paint().let {
                it.style = Paint.Style.FILL
                it.strokeJoin = Paint.Join.ROUND
                it.strokeWidth = 100f
                it.strokeCap = Paint.Cap.ROUND
                it.color = Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.blendMode = BlendMode.CLEAR
                }
                it
            }

            binding.drawingCv.post {
                val firstX = ((drawing!!.tutorialPoints.first().xPercent * binding.drawingCv.width) / 100) + binding.drawingCv.x
                val firstY = ((drawing!!.tutorialPoints.first().yPercent * binding.drawingCv.height) / 100) + binding.drawingCv.y
                overlayCanvas.setBitmap(overlayBitmap)
                overlayCanvas.drawColor(Color.argb(60, 0, 0, 0))
                overlayCanvas.drawCircle(
                    firstX,
                    firstY,
                    50f,
                    overlayPaint
                )

                binding.hand.post {
                    binding.hand.x = firstX - binding.hand.width - 50
                    binding.hand.y = firstY - 60
                }

                binding.hand.visibility = View.VISIBLE
            }

            var startX = -1f
            var startY = -1f

            val duration = 500L

            binding.overlayIv.setImageBitmap(overlayBitmap)

            val step = 5
            val durationStep = ((100 - step) / step) + 1

            lifecycleScope.launch {
                drawing!!.tutorialPoints.forEach { point ->
                    val stopX = ((point.xPercent * binding.drawingCv.width) / 100) + binding.drawingCv.x
                    val stopY = ((point.yPercent * binding.drawingCv.height) / 100) + binding.drawingCv.y

                    if (startX == -1f || startY == -1f) {
                        startX = stopX
                        startY = stopY
                        return@forEach
                    }

                    if (point.goToType == GoToType.JumpTo) {
                        delay(duration / durationStep)
                        overlayBitmap = Bitmap.createBitmap(
                            binding.overlayIv.width,
                            binding.overlayIv.height,
                            Bitmap.Config.ARGB_8888
                        )
                        overlayCanvas.setBitmap(overlayBitmap)
                        overlayCanvas.drawColor(Color.argb(60, 0, 0, 0))
                        overlayCanvas.drawCircle(
                            stopX,
                            stopY,
                            50f,
                            overlayPaint
                        )
                        binding.overlayIv.setImageBitmap(overlayBitmap)

                        binding.hand.post {
                            binding.hand.x = stopX - binding.hand.width - 50
                            binding.hand.y = stopY - 60
                        }

                        startX = stopX
                        startY = stopY

                        return@forEach
                    }

                    for (i in step..100 step step) {
                        delay(duration / durationStep)
                        val progressX = ((stopX - startX) * i) / 100f
                        val currentX = startX + progressX

                        val progressY = ((stopY - startY) * i) / 100f
                        val currentY = startY + progressY

                        overlayBitmap = Bitmap.createBitmap(
                            binding.overlayIv.width,
                            binding.overlayIv.height,
                            Bitmap.Config.ARGB_8888
                        )
                        overlayCanvas.setBitmap(overlayBitmap)
                        overlayCanvas.drawColor(Color.argb(60, 0, 0, 0))
                        overlayCanvas.drawCircle(
                            currentX,
                            currentY,
                            50f,
                            overlayPaint
                        )
                        binding.overlayIv.setImageBitmap(overlayBitmap)

                        binding.hand.post {
                            binding.hand.x = currentX - binding.hand.width - 50
                            binding.hand.y = currentY - 60
                        }
                    }

                    startX = stopX
                    startY = stopY
                }
            }

        }
    }

    private fun initUndoRedoButtons() {
        binding.undo.setOnClickListener {
            if (currentBitmapIndex > 0) {
                currentBitmapIndex--
                resetBitmap()
                updateUndoRedoButtonsState()
            }
        }
        binding.redo.setOnClickListener {
            if (currentBitmapIndex < bitmapList.lastIndex) {
                currentBitmapIndex++
                resetBitmap()
                updateUndoRedoButtonsState()
            }
        }
    }

    private fun updateUndoRedoButtonsState() {
        binding.undo.isEnabled = currentBitmapIndex > 0
        binding.redo.isEnabled = currentBitmapIndex < bitmapList.lastIndex
    }

    private fun initColorPicker() {
        closeColorPicker()

        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )

        val colors = listOf(
            R.color.red,
            R.color.green,
            R.color.blue,
            R.color.orange
        )

        val selectedColorIndex = colors.indexOf(selectedColor)
        onColorSelectedAction(colorButtons[selectedColorIndex], selectedColor)

        colorButtons.zip(colors).forEach { buttonColor ->
            buttonColor.first.setOnClickListener {
                onColorSelectedAction(buttonColor.first, buttonColor.second)
            }
        }
    }

    private fun onColorSelectedAction(button: Button, color: Int) {
        selectColor(color)
        resetColorButtons()
        button
            .animate()
            .scaleX(ANIMATION_SCALE)
            .scaleY(ANIMATION_SCALE)
            .setDuration(ANIMATION_DURATION)
            .start()
    }

    private fun selectColor(color: Int) {
        selectedColor = color

        brushPaint.color =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                resources.getColor(color, null)
            else
                resources.getColor(color)

        binding.selectedColorDot.setBackgroundColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(color, null)
            } else {
                resources.getColor(color)
            }
        )
    }

    private fun resetColorButtons() {
        val buttons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )

        buttons.forEach { button ->
            if (button.scaleX == 1f) return@forEach

            button
                .animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIMATION_DURATION)
                .start()
        }
    }

    private fun initToolButtons() {
        onToolSelected(binding.brush, Tool.Brush)

        val toolButtons = listOf(
            binding.brush,
            binding.pail,
            binding.eraser,
            binding.colors
        )

        val tools = listOf(
            Tool.Brush,
            Tool.Pail,
            Tool.Eraser,
            Tool.Colors
        )

        toolButtons.zip(tools).forEach { buttonTool ->
            buttonTool.first.setOnClickListener {
                when (buttonTool.second) {
                    Tool.Colors ->
                        onColorsToolSelected(buttonTool.first, buttonTool.second)
                    Tool.Brush, Tool.Eraser ->
                        onSliderToolSelected(buttonTool.first, buttonTool.second)
                    else ->
                        onToolSelected(buttonTool.first, buttonTool.second)
                }
            }
        }
    }

    private fun onSliderToolSelected(view: View, tool: Tool) {
        onToolSelected(view, tool)
        openSlider()
    }

    private fun onColorsToolSelected(view: View, tool: Tool) {
        onToolSelected(view, tool)
        openColorPicker()
    }

    private fun onToolSelected(view: View, tool: Tool) {
        selectedTool = tool
        resetToolButtons()
        view.isActivated = true
        closeColorPicker()
        closeSlider()
        updateSliderState()
    }

    private fun resetToolButtons() {
        val buttons = listOf(
            binding.brush,
            binding.pail,
            binding.eraser,
            binding.colors
        )

        buttons.forEach { button ->
            button.isActivated = false
        }
    }

    private fun initSlider() {
        updateSliderState()

        binding.slider.addOnChangeListener { _, value, _ ->
            if (selectedTool == Tool.Brush) {
                selectedBrushSize = value
                brushPaint.strokeWidth = selectedBrushSize * 2
            } else if (selectedTool == Tool.Eraser) {
                selectedEraserSize = value
                eraserPaint.strokeWidth = selectedEraserSize * 2
            }
        }
    }

    private fun updateSliderState() {
        binding.slider.visibility =
            when (selectedTool) {
                Tool.Brush -> {
                    binding.slider.value = selectedBrushSize
                    View.VISIBLE
                }
                Tool.Eraser -> {
                    binding.slider.value = selectedEraserSize
                    View.VISIBLE
                }
                else -> {
                    View.INVISIBLE
                }
            }
    }

    private fun clearBitmap() {
        binding.canvasIv.post {
            editBitmap(
                Bitmap.createBitmap(
                    binding.canvasIv.width,
                    binding.canvasIv.height,
                    Bitmap.Config.ARGB_8888
                )
            )
        }
    }

    private fun editBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap
        bitmapList.add(newBitmap.copy(newBitmap.config, newBitmap.isMutable))
        canvas.setBitmap(bitmap)
    }

    private fun resetBitmap() {
        bitmap =  Bitmap.createBitmap(
            binding.canvasIv.width,
            binding.canvasIv.height,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        canvas.drawBitmap(bitmapList[currentBitmapIndex], Matrix(Matrix()), null)
        binding.canvasIv.setImageBitmap(bitmap)
    }

    private fun addCurrentBitmapToHistory() {
        bitmap?.let { bitmap ->
            if (currentBitmapIndex != bitmapList.lastIndex) {
                for (i in bitmapList.lastIndex downTo (currentBitmapIndex + 1)) {
                    Log.d("indexes", "remove at $i")
                    bitmapList.removeAt(i)
                }
            }

            bitmapList.add(
                binding.canvasIv.drawToBitmap(
                    Bitmap.Config.ARGB_8888
                )
            )
            currentBitmapIndex++
            updateUndoRedoButtonsState()
        }
    }

    private fun drawWithBrushAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                canvas.drawCircle(event.x, event.y, selectedBrushSize, brushPaint)
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                canvas.drawLine(
                    lastBrushPoint!!.x.toFloat(),
                    lastBrushPoint!!.y.toFloat(),
                    event.x,
                    event.y,
                    brushPaint
                )
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun drawWithPailAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                canvas.drawColor(brushPaint.color)
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun eraseAction(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                canvas.drawCircle(event.x, event.y, selectedEraserSize, eraserPaint)
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                canvas.drawLine(
                    lastBrushPoint!!.x.toFloat(),
                    lastBrushPoint!!.y.toFloat(),
                    event.x,
                    event.y,
                    eraserPaint
                )
                lastBrushPoint = Point(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                addCurrentBitmapToHistory()
            }
        }
    }

    private fun openColorPicker() {
        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )
        colorButtons.forEach { button ->
            button.visibility = View.VISIBLE
        }
    }

    private fun closeColorPicker() {
        val colorButtons = listOf(
            binding.colorOne,
            binding.colorTwo,
            binding.colorThree,
            binding.colorFour
        )
        colorButtons.forEach { button ->
            button.visibility = View.INVISIBLE
        }
    }

    private fun openSlider() {
        binding.slider.visibility = View.VISIBLE
    }

    private fun closeSlider() {
        binding.slider.visibility = View.INVISIBLE
    }

    private fun onSaveAction() {
        val backgroundBitmap = Bitmap.createBitmap(binding.canvasIv.width, binding.canvasIv.height, Bitmap.Config.ARGB_8888)
        val drawingBitmap = Bitmap.createBitmap(binding.canvasIv.width, binding.canvasIv.height, Bitmap.Config.ARGB_8888)
        val outlineBimap = Bitmap.createBitmap(binding.canvasIv.width, binding.canvasIv.height, Bitmap.Config.ARGB_8888)

        val backgroundCanvas = Canvas(backgroundBitmap)
        val drawingCanvas = Canvas(drawingBitmap)
        val outlineCanvas = Canvas(outlineBimap)

        backgroundCanvas.setBitmap(backgroundBitmap)
        drawingCanvas.setBitmap(drawingBitmap)
        outlineCanvas.setBitmap(outlineBimap)

        binding.emptyBoardIv.draw(backgroundCanvas)
        binding.canvasIv.draw(drawingCanvas)
        binding.drawingIv.draw(outlineCanvas)

        val saveBitmap = Bitmap.createBitmap(binding.canvasIv.width, binding.canvasIv.height, Bitmap.Config.ARGB_8888)
        val saveCanvas = Canvas(saveBitmap)

        saveCanvas.drawBitmap(backgroundBitmap, Matrix(), null)
        saveCanvas.drawBitmap(drawingBitmap, Matrix(), null)
        saveCanvas.drawBitmap(outlineBimap, Matrix(), null)

        val filePath = drawingsDir
        File(filePath).mkdirs()
        val file =
            if (args.filePath != null)
                File(args.filePath!!)
            else if (args.drawingId == -1)
                File(filePath, "${UUID.randomUUID()}-0.jpg")
            else
                File(filePath, "${UUID.randomUUID()}-${args.drawingId}.jpg")

        try {
            val isSaved = saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
            if (isSaved) {
                Toast.makeText(requireContext(), "Image saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ANIMATION_DURATION: Long = 200
        private const val ANIMATION_SCALE = 1.3f
    }
}