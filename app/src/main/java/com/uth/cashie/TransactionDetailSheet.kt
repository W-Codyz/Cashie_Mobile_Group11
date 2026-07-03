package com.uth.cashie

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uth.cashie.adapter.TransactionAdapter.Companion.formatVND
import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.databinding.DialogTransactionDetailBinding
import com.uth.cashie.model.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailSheet : BottomSheetDialogFragment() {

    private var _binding: DialogTransactionDetailBinding? = null
    private val binding get() = _binding!!

    var onEditRequested: ((transactionId: Int) -> Unit)? = null
    var onDeleted: (() -> Unit)? = null

    private val displayDateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val storedDateFmt  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getInt(ARG_ID, -1) ?: -1

        lifecycleScope.launch {
            val tx = TransactionRepository.getById(id)
            if (tx == null) {
                dismissAllowingStateLoss()
                return@launch
            }

            bindTransaction(tx)

            binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }

            binding.btnEditTransaction.setOnClickListener {
                onEditRequested?.invoke(tx.id)
                dismissAllowingStateLoss()
            }

            binding.btnDeleteTransaction.setOnClickListener {
                confirmDelete(tx)
            }
        }
    }

    private fun bindTransaction(tx: Transaction) {
        binding.tvDetailEmoji.text = tx.emoji
        runCatching {
            val base = Color.parseColor(tx.iconColorHex)
            binding.tvDetailEmoji.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(35, Color.red(base), Color.green(base), Color.blue(base)))
            }
        }

        if (tx.isIncome) {
            binding.tvDetailTypeBadge.text = getString(R.string.label_type_badge_income)
            binding.tvDetailTypeBadge.setBackgroundColor(Color.parseColor("#22CC00"))
        } else {
            binding.tvDetailTypeBadge.text = getString(R.string.label_type_badge_expense)
            binding.tvDetailTypeBadge.setBackgroundColor(Color.parseColor("#FF4444"))
        }

        val sign  = if (tx.isIncome) "+" else "-"
        val color = if (tx.isIncome) Color.parseColor("#22CC00") else Color.parseColor("#FF4444")
        binding.tvDetailAmount.text = "$sign${formatVND(tx.amount)}"
        binding.tvDetailAmount.setTextColor(color)

        binding.tvDetailTitle.text    = tx.title
        binding.tvDetailCategory.text = "${tx.emoji}  ${tx.category}"

        val displayDate = runCatching {
            val parsed = storedDateFmt.parse(tx.date)
            if (parsed != null) displayDateFmt.format(parsed) else tx.date
        }.getOrDefault(tx.date)
        binding.tvDetailDate.text = displayDate
        binding.tvDetailTime.text = tx.time

        if (tx.title.isNotBlank() && tx.title != tx.category) {
            binding.layoutDetailNote.visibility = View.VISIBLE
            binding.tvDetailNote.text = tx.title
        } else {
            binding.layoutDetailNote.visibility = View.GONE
        }
    }

    private fun confirmDelete(tx: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_msg))
            .setPositiveButton(getString(R.string.btn_confirm)) { _, _ ->
                lifecycleScope.launch {
                    TransactionRepository.delete(tx.id)
                    onDeleted?.invoke()
                    dismissAllowingStateLoss()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "arg_transaction_id"

        fun newInstance(transactionId: Int) = TransactionDetailSheet().apply {
            arguments = Bundle().apply { putInt(ARG_ID, transactionId) }
        }
    }
}
