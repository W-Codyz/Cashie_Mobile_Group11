package com.uth.cashie.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uth.cashie.databinding.ItemTransactionBinding
import com.uth.cashie.databinding.ItemTransactionHeaderBinding
import com.uth.cashie.model.Transaction
import com.uth.cashie.model.TransactionGroup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs

class TransactionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class ListItem {
        data class Header(val group: TransactionGroup) : ListItem()
        data class Item(val transaction: Transaction) : ListItem()
    }

    private val items = mutableListOf<ListItem>()

    fun submitGroups(groups: List<TransactionGroup>) {
        items.clear()
        groups.forEach { group ->
            items.add(ListItem.Header(group))
            group.transactions.forEach { items.add(ListItem.Item(it)) }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ListItem.Header -> VIEW_HEADER
        is ListItem.Item   -> VIEW_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_HEADER -> HeaderViewHolder(ItemTransactionHeaderBinding.inflate(inflater, parent, false))
            else        -> ItemViewHolder(ItemTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item.group)
            is ListItem.Item   -> (holder as ItemViewHolder).bind(item.transaction)
        }
    }

    override fun getItemCount() = items.size

    // ── Header ViewHolder ────────────────────────────────────────────────────
    class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: TransactionGroup) {
            binding.tvDateLabel.text = group.dateLabel
            val sign  = if (group.dayNet >= 0) "+" else "-"
            val color = if (group.dayNet >= 0) Color.parseColor("#22CC00") else Color.parseColor("#FF4444")
            binding.tvDayNet.text      = "$sign${formatVND(group.dayNet)}"
            binding.tvDayNet.setTextColor(color)
        }
    }

    // ── Item ViewHolder ──────────────────────────────────────────────────────
    class ItemViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvEmoji.text = transaction.emoji

            // Each item gets its own oval drawable to avoid shared-drawable tint conflicts
            runCatching {
                val base = Color.parseColor(transaction.iconColorHex)
                val tinted = Color.argb(32, Color.red(base), Color.green(base), Color.blue(base))
                binding.tvEmoji.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(tinted)
                }
            }

            binding.tvTitle.text        = transaction.title
            binding.tvCategoryTime.text = "${transaction.category}  ·  ${transaction.time}"

            val sign  = if (transaction.isIncome) "+" else "-"
            val color = if (transaction.isIncome) Color.parseColor("#22CC00") else Color.parseColor("#FF4444")
            binding.tvAmount.text = "$sign${formatVND(transaction.amount)}"
            binding.tvAmount.setTextColor(color)
        }
    }

    companion object {
        private const val VIEW_HEADER = 0
        private const val VIEW_ITEM   = 1

        fun formatVND(amount: Long): String {
            val sym = DecimalFormatSymbols().apply {
                groupingSeparator = '.'
                decimalSeparator  = ','
            }
            return DecimalFormat("#,###", sym).format(abs(amount)) + "đ"
        }
    }
}
