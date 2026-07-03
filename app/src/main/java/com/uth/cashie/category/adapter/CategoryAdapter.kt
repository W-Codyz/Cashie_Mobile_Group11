package com.uth.cashie.category.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.ThemeManager
import com.uth.cashie.category.model.Category
import com.uth.cashie.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit,
    private val onItemLongClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onItemClick: (Category) -> Unit,
        private val onItemLongClick: (Category) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.txtName.text = category.name
            // Hiển thị emoji trực tiếp, fallback về 📂 nếu trống
            binding.tvEmoji.text = category.emoji.ifEmpty { "📂" }

            // category.color đã là parsed color Int từ repository (Color.parseColor)
            val colorInt = if (category.color != 0) category.color
                           else ThemeManager.getThemeColorInt()

            binding.flIconBg.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(colorInt)
            }

            binding.root.setOnClickListener { onItemClick(category) }
            binding.root.setOnLongClickListener { onItemLongClick(category); true }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
}
