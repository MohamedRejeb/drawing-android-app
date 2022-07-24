package com.mohamedbenrejeb.drawapplication.models

import java.io.File
import java.util.*

data class SavedDrawing(
    val id: String = UUID.randomUUID().toString(),
    val file: File,
    val isSelected: Boolean = false,
    val isSelectionActive: Boolean = false
)
