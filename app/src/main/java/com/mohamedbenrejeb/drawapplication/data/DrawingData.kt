package com.mohamedbenrejeb.drawapplication.data

import com.mohamedbenrejeb.drawapplication.R
import com.mohamedbenrejeb.drawapplication.models.Drawing
import com.mohamedbenrejeb.drawapplication.models.DrawingType
import com.mohamedbenrejeb.drawapplication.models.GoToType
import com.mohamedbenrejeb.drawapplication.models.TutorialPoint

object DrawingData {
    private val drawingList = listOf(
        Drawing(
            id = 1,
            text = "Number One",
            imageResource = R.drawable.number1,
            type = DrawingType.Number,
            tutorialPoints = listOf(
                TutorialPoint(41.55f, 33f, GoToType.AnimationTo),
                TutorialPoint(48f, 28.65f, GoToType.AnimationTo),
                TutorialPoint(48f, 67.93f, GoToType.AnimationTo),
                TutorialPoint(40.31f, 67.52f, GoToType.AnimationTo),
                TutorialPoint(53.31f, 67.52f, GoToType.AnimationTo)
            )
        ),
        Drawing(
            id = 2,
            text = "Number Two",
            imageResource = R.drawable.number2,
            type = DrawingType.Number
        ),
        Drawing(
            id = 3,
            text = "Number Three",
            imageResource = R.drawable.number3,
            type = DrawingType.Number
        ),
        Drawing(
            id = 4,
            text = "Number Four",
            imageResource = R.drawable.number4,
            type = DrawingType.Number
        ),

        Drawing(
            id = 5,
            text = "Letter A",
            imageResource = R.drawable.lettera,
            type = DrawingType.Letter
        ),
        Drawing(
            id = 6,
            text = "Letter B",
            imageResource = R.drawable.letterb,
            type = DrawingType.Letter,
            emptyList()
        ),
        Drawing(
            id = 7,
            text = "Letter C",
            imageResource = R.drawable.letterc,
            type = DrawingType.Letter,
            emptyList()
        ),
        Drawing(
            id = 8,
            text = "Letter D",
            imageResource = R.drawable.letterd,
            type = DrawingType.Letter,
            emptyList()
        ),

        Drawing(
            id = 9,
            text = "Leopard",
            imageResource = R.drawable.leopard,
            type = DrawingType.Animal
        ),
        Drawing(
            id = 10,
            text = "Dear",
            imageResource = R.drawable.deer,
            type = DrawingType.Animal
        )
    )

    fun getDrawingById(id: Int): Drawing? {
        return drawingList.firstOrNull { it.id == id }
    }

    fun getDrawingListByType(drawingType: DrawingType): List<Drawing> {
        return drawingList.filter { it.type == drawingType }
    }
}