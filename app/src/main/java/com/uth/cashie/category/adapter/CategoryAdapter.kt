package com.uth.cashie.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.category.model.Category
import com.uth.cashie.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit,        // Click 1 lần
    private val onItemLongClick: (Category) -> Unit      // Giữ lâu
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

            if (category.icon != 0) {
                binding.imgIcon.setImageResource(category.icon)
            } else {
                binding.imgIcon.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            val colorInt = if (category.color != 0) {
                try {
                    ContextCompat.getColor(binding.root.context, category.color)
                } catch (e: Exception) {
                    category.color
                }
            } else {
                com.uth.cashie.ThemeManager.getThemeColorInt()
            }

            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(colorInt)
            }
            binding.imgIcon.background = bg
            binding.imgIcon.setColorFilter(android.graphics.Color.WHITE)

            // Click 1 lần → vào chi tiết
            binding.root.setOnClickListener {
                onItemClick(category)
            }

            // Giữ lâu → hiện dialog xóa
            binding.root.setOnLongClickListener {
                onItemLongClick(category)
                true
            }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}