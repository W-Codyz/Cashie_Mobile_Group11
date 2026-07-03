package com.uth.cashie.category.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.databinding.ItemColorSelectBinding

class ColorSelectAdapter(
    private var colors: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<ColorSelectAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColorSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onSelect) { position ->
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    fun updateList(newColors: List<String>) {
        colors = newColors
        selectedPosition = 0
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemColorSelectBinding,
        private val onSelect: (String) -> Unit,
        private val onSelectionChanged: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(colorHex: String, isSelected: Boolean) {
            try {
                val color = Color.parseColor(colorHex)
                binding.vwColor.setBackgroundColor(color)
            } catch (e: Exception) {
                binding.vwColor.setBackgroundColor(Color.BLACK)
            }

            val density = binding.root.resources.displayMetrics.density
            if (isSelected) {
                binding.ivCheck.visibility = android.view.View.VISIBLE
                binding.cardColor.strokeWidth = (density * 3).toInt()
                binding.cardColor.elevation = 10f
            } else {
                binding.ivCheck.visibility = android.view.View.GONE
                binding.cardColor.strokeWidth = 0
                binding.cardColor.elevation = 2f
            }

            binding.root.setOnClickListener {
                onSelect(colorHex)
                onSelectionChanged(adapterPosition)
            }
        }
    }
}
