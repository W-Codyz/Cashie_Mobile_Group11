package com.uth.cashie.category.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uth.cashie.category.model.Category
import com.uth.cashie.databinding.FragmentCategoryDetailBinding

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

        // Lấy dữ liệu từ Bundle
        val id = arguments?.getInt("id") ?: 0
        val name = arguments?.getString("name") ?: ""
        val icon = arguments?.getInt("icon") ?: 0
        val color = arguments?.getInt("color") ?: 0
        val emoji = arguments?.getString("emoji") ?: ""
        val isDefault = arguments?.getBoolean("isDefault") ?: false

        // Tạo đối tượng Category từ các trường đã lấy
        val category = Category(id, name, icon, color, emoji, isDefault)

        // Set toolbar
        binding.toolbarDetail.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Hiển thị thông tin danh mục
        binding.tvEmoji.text = category.emoji
        binding.tvName.text = category.name
        binding.tvType.text = "Loại: ${if (category.icon == 1) "Thu nhập" else "Chi phí"}"
        binding.tvColor.text = "Màu: #${Integer.toHexString(category.color).uppercase()}"

        // Tạm thời ẩn RecyclerView vì chưa có dữ liệu giao dịch
        binding.rvTransactions.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE

        // TODO: Sau này sẽ load danh sách giao dịch theo category.id
    }
}