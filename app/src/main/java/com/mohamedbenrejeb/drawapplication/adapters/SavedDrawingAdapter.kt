package com.mohamedbenrejeb.drawapplication.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mohamedbenrejeb.drawapplication.R
import com.mohamedbenrejeb.drawapplication.databinding.DrawingItemBinding
import com.mohamedbenrejeb.drawapplication.models.SavedDrawing

class SavedDrawingAdapter(
    private val onDrawingSingleClick: (savedDrawing: SavedDrawing) -> Unit,
    private val onDrawingLongClick: (savedDrawing: SavedDrawing) -> Unit
): ListAdapter<SavedDrawing, SavedDrawingAdapter.SavedDrawingVH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedDrawingVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DrawingItemBinding.inflate(layoutInflater, parent, false)
        return SavedDrawingVH(binding)
    }

    override fun onBindViewHolder(holder: SavedDrawingVH, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
        holder: SavedDrawingVH,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            when(payloads[0]) {
                UPDATE_SELECTION -> holder.update(position)
            }
        }
    }

    private class DiffCallback: DiffUtil.ItemCallback<SavedDrawing>() {
        override fun areItemsTheSame(oldItem: SavedDrawing, newItem: SavedDrawing): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedDrawing, newItem: SavedDrawing): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: SavedDrawing, newItem: SavedDrawing): Any? {
            if (oldItem.isSelected != newItem.isSelected || oldItem.isSelectionActive != newItem.isSelectionActive)
                return UPDATE_SELECTION

            return super.getChangePayload(oldItem, newItem)
        }
    }

    inner class SavedDrawingVH(
        private val binding: DrawingItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val savedDrawing = getItem(position)

            binding.cardView.setOnClickListener {
                onDrawingSingleClick(savedDrawing)
            }

            binding.cardView.setOnLongClickListener {
                onDrawingLongClick(savedDrawing)
                true
            }

            val bitmap = BitmapFactory.decodeFile(savedDrawing.file.absolutePath)
            binding.drawingIv.setImageBitmap(bitmap)
        }

        fun update(position: Int) {
            val savedDrawing = getItem(position)

            Log.d("adapter", "update $position")

            binding.radioBtn.setImageResource(
                if (savedDrawing.isSelected) R.drawable.ic_radio_checked
                else R.drawable.ic_radio_unchecked
            )

            binding.radioBtn.visibility =
                if (savedDrawing.isSelectionActive)
                    View.VISIBLE
                else
                    View.GONE
        }
    }

    companion object {
        private const val UPDATE_SELECTION = 1
    }
}

