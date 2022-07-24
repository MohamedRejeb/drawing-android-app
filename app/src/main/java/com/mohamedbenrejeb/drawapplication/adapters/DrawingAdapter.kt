package com.mohamedbenrejeb.drawapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mohamedbenrejeb.drawapplication.databinding.DrawingItemBinding
import com.mohamedbenrejeb.drawapplication.models.Drawing

class DrawingAdapter(
    private val drawingList: List<Drawing>,
    private val navigateToDrawing: (drawing: Drawing) -> Unit
): RecyclerView.Adapter<DrawingAdapter.DrawingVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawingVH {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DrawingItemBinding.inflate(layoutInflater, parent, false)
        return DrawingVH(binding)
    }

    override fun onBindViewHolder(holder: DrawingVH, position: Int) {
        val drawing = drawingList[position]
        holder.bind(drawing)
    }

    override fun getItemCount(): Int {
        return drawingList.size
    }

    inner class DrawingVH(
        private val binding: DrawingItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(drawing: Drawing) {
            binding.cardView.setOnClickListener {
                navigateToDrawing(drawing)
            }
            binding.drawingIv.setImageResource(drawing.imageResource)
        }
    }
}

