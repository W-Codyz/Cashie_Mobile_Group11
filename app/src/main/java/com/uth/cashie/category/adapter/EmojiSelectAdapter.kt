package com.uth.cashie.category.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.ThemeManager
import com.uth.cashie.databinding.ItemEmojiSelectBinding
import com.uth.cashie.model.EmojiCategory

class EmojiSelectAdapter(
    private var emojis: List<EmojiCategory>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<EmojiSelectAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmojiSelectBinding.inflate(
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
        holder.bind(emojis[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = emojis.size

    fun updateList(newEmojis: List<EmojiCategory>) {
        emojis = newEmojis
        selectedPosition = 0
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemEmojiSelectBinding,
        private val onSelect: (String) -> Unit,
        private val onSelectionChanged: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: EmojiCategory, isSelected: Boolean) {
            binding.tvEmoji.text = emoji.emoji
            binding.tvName.text = emoji.name

            val themeColor = ThemeManager.getThemeColorInt()
            if (isSelected) {
                // Dùng binding.root nếu root là CardView, nếu không hãy đổi thành binding.cardView
                (binding.root as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(themeColor)
                binding.tvEmoji.setTextColor(ColorStateList.valueOf(0xFFFFFFFF.toInt()))
                binding.tvName.setTextColor(ColorStateList.valueOf(0xFFFFFFFF.toInt()))
            } else {
                (binding.root as? androidx.cardview.widget.CardView)?.setCardBackgroundColor(0xFFF5F5F5.toInt())
                binding.tvEmoji.setTextColor(ColorStateList.valueOf(0xFF000000.toInt()))
                binding.tvName.setTextColor(ColorStateList.valueOf(0xFF000000.toInt()))
            }

            binding.root.setOnClickListener {
                onSelect(emoji.emoji)
                onSelectionChanged(adapterPosition)
            }
        }
    }
}