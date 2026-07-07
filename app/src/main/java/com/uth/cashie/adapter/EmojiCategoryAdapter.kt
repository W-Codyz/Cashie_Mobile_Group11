package com.uth.cashie.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.ThemeManager
import com.uth.cashie.databinding.ItemEmojiCategoryBinding
import com.uth.cashie.model.EmojiCategory

class EmojiCategoryAdapter(
    private val onSelected: (EmojiCategory) -> Unit
) : RecyclerView.Adapter<EmojiCategoryAdapter.VH>() {

    private val items = mutableListOf<EmojiCategory>()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun submitList(list: List<EmojiCategory>) {
        items.clear()
        items.addAll(list)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    /** Đặt lại selection theo tên danh mục (dùng khi load dữ liệu edit) */
    fun selectByName(name: String) {
        val idx = items.indexOfFirst { it.name == name }
        if (idx >= 0) {
            selectedPosition = idx
            notifyDataSetChanged()
        }
    }

    fun getSelected(): EmojiCategory? =
        if (selectedPosition >= 0 && selectedPosition < items.size) items[selectedPosition] else null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEmojiCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], position == selectedPosition)

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemEmojiCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cat: EmojiCategory, isSelected: Boolean) {
            binding.tvCategoryEmoji.text = cat.emoji
            binding.tvCategoryName.text = cat.name

            // Màu nền vòng tròn: tint nhẹ từ màu chủ đề, border khi selected
            val base = ThemeManager.getThemeColorInt()
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                if (isSelected) {
                    setColor(Color.argb(40, Color.red(base), Color.green(base), Color.blue(base)))
                    setStroke(4, base)
                } else {
                    setColor(Color.argb(20, Color.red(base), Color.green(base), Color.blue(base)))
                }
            }
            binding.tvCategoryEmoji.background = bg

            binding.tvCategoryName.setTextColor(
                if (isSelected) base
                else 0xFF888888.toInt()
            )

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = selectedPosition
                selectedPosition = pos
                if (prev >= 0) notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onSelected(cat)
            }
        }
    }
}
