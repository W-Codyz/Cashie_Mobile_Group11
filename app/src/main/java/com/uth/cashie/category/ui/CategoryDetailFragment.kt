package com.uth.cashie.category.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.cashie.adapter.TransactionAdapter
import com.uth.cashie.category.model.Category
import com.uth.cashie.data.TransactionRepository
import com.uth.cashie.database.SessionManager
import com.uth.cashie.databinding.FragmentCategoryDetailBinding
import kotlinx.coroutines.launch

class CategoryDetailFragment : Fragment() {

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(
            id: Int,
            name: String,
            icon: Int,
            color: Int,
            emoji: String,
            isDefault: Boolean
        ): CategoryDetailFragment {
            val fragment = CategoryDetailFragment()
            val args = Bundle()
            args.putInt("id", id)
            args.putString("name", name)
            args.putInt("icon", icon)
            args.putInt("color", color)
            args.putString("emoji", emoji)
            args.putBoolean("isDefault", isDefault)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getInt("id") ?: 0
        val name = arguments?.getString("name") ?: ""
        val icon = arguments?.getInt("icon") ?: 0
        val color = arguments?.getInt("color") ?: 0
        val emoji = arguments?.getString("emoji") ?: ""
        val isDefault = arguments?.getBoolean("isDefault") ?: false

        val category = Category(id, name, icon, color, emoji, isDefault)

        binding.toolbarDetail.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.tvEmoji.text = category.emoji
        binding.tvName.text = category.name
        binding.tvType.text = "Loại: ${if (category.icon == 1) "Thu nhập" else "Chi phí"}"
        binding.tvColor.text = "Màu: #${Integer.toHexString(category.color).uppercase()}"

        val adapter = TransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter

        SessionManager.init(requireContext())
        TransactionRepository.init(requireContext())

        lifecycleScope.launch {
            val transactions = TransactionRepository.getByCategory(id.toLong())
            val groups = TransactionRepository.getGroupedByDate(transactions)
            if (groups.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                adapter.submitGroups(groups)
            }
        }
    }
}
