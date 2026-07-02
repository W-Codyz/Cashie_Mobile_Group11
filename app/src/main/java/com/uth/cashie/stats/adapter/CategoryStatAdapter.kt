package com.uth.cashie.stats.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.R
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.stats.model.CategoryStat

class CategoryStatAdapter : ListAdapter<CategoryStat, CategoryStatAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView    = view.findViewById(R.id.tvCatEmoji)
        val tvName: TextView     = view.findViewById(R.id.tvCatName)
        val tvAmount: TextView   = view.findViewById(R.id.tvCatAmount)
        val tvPercent: TextView  = view.findViewById(R.id.tvCatPercent)
        val barFill: View        = view.findViewById(R.id.barFill)
        val barBg: View          = view.findViewById(R.id.barBg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_category_stat, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvEmoji.text   = item.emoji
        holder.tvName.text    = item.categoryName
        holder.tvAmount.text  = formatVND(item.totalAmount)
        holder.tvPercent.text = "${"%.1f".format(item.percentage)}%"

        // Animate bar width theo phần trăm
        holder.barBg.post {
            val totalWidth = holder.barBg.width
            val fillWidth  = (totalWidth * item.percentage / 100f).toInt().coerceAtLeast(4)
            val lp = holder.barFill.layoutParams
            lp.width = fillWidth
            holder.barFill.layoutParams = lp
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryStat>() {
            override fun areItemsTheSame(a: CategoryStat, b: CategoryStat) =
                a.categoryName == b.categoryName
            override fun areContentsTheSame(a: CategoryStat, b: CategoryStat) = a == b
        }
    }
}
