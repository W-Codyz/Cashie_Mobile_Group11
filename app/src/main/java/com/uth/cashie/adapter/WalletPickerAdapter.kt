package com.uth.cashie.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.uth.cashie.R
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.WalletEntity

class WalletPickerAdapter(
    private val items: List<WalletItem>,
    private val themeColor: Int,
    private val onSelected: (WalletItem) -> Unit
) : RecyclerView.Adapter<WalletPickerAdapter.VH>() {

    data class WalletItem(
        val wallet: WalletEntity,
        val balance: Long
    )

    private var selectedId: Long? = null

    fun setSelectedId(id: Long?) {
        selectedId = id
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView     = itemView as MaterialCardView
        val tvEmoji: TextView          = itemView.findViewById(R.id.tvWalletEmoji)
        val tvName: TextView           = itemView.findViewById(R.id.tvWalletName)
        val tvType: TextView           = itemView.findViewById(R.id.tvWalletType)
        val tvBalance: TextView        = itemView.findViewById(R.id.tvWalletBalance)
        val tvWarning: TextView        = itemView.findViewById(R.id.tvLowBalanceWarning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_picker, parent, false)
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item       = items[position]
        val isSelected = item.wallet.id == selectedId
        val isLow      = item.balance < 200_000L
        val currency   = SessionManager.getCurrency()

        holder.tvEmoji.text   = item.wallet.iconEmoji
        holder.tvName.text    = item.wallet.name
        holder.tvType.text    = when (item.wallet.type) {
            "cash" -> "Tiền mặt"
            "bank" -> "Ngân hàng"
            else   -> "Ví khác"
        }

        holder.tvBalance.text = if (item.balance < 0) "-${formatVND(item.balance, currency)}"
                               else formatVND(item.balance, currency)
        holder.tvBalance.setTextColor(if (isLow || item.balance < 0) Color.parseColor("#FF4444") else themeColor)
        holder.tvWarning.visibility = if (isLow) View.VISIBLE else View.GONE

        // Emoji circle background — theme color with alpha
        holder.tvEmoji.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(30, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)))
        }

        // Card selected state
        if (isSelected) {
            holder.card.strokeColor = themeColor
            holder.card.setCardBackgroundColor(
                Color.argb(18, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor))
            )
        } else {
            holder.card.strokeColor = Color.parseColor("#E0E0E0")
            holder.card.setCardBackgroundColor(Color.WHITE)
        }

        holder.card.setOnClickListener {
            val prevPos = items.indexOfFirst { it.wallet.id == selectedId }
            selectedId = item.wallet.id
            if (prevPos >= 0) notifyItemChanged(prevPos)
            notifyItemChanged(position)
            onSelected(item)
        }
    }
}
