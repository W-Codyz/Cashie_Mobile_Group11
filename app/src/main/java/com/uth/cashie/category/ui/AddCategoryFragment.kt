package com.uth.cashie.category.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.cashie.ThemeManager
import com.uth.cashie.category.adapter.EmojiSelectAdapter
import com.uth.cashie.category.adapter.ColorSelectAdapter
import com.uth.cashie.category.data.CategoryRepository
import com.uth.cashie.databinding.FragmentAddCategoryBinding
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.database.entity.CategoryEntity
import com.uth.cashie.model.EmojiCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddCategoryFragment : Fragment() {

    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoryViewModel

    private var selectedType = "expense"
    private var selectedEmoji = "🍜"
    private var selectedColor = "#FF8C00"

    private lateinit var emojiAdapter: EmojiSelectAdapter
    private lateinit var colorAdapter: ColorSelectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            parentFragment?.childFragmentManager?.popBackStack()
        }

        try {
            val database = CashieDatabase.getInstance(requireContext())
            val repository = CategoryRepository(database.categoryDao())
            SessionManager.init(requireContext())
            val userId = SessionManager.getCurrentUserId()
            val actualUserId = if (userId == -1L) 1L else userId

            val factory = CategoryViewModelFactory(repository, actualUserId)
            val vm: CategoryViewModel by viewModels { factory }
            viewModel = vm

            applyTheme()
            setupTypeButtons()
            setupEmojiRecyclerView()
            setupColorRecyclerView()
            setupActionButtons()

        } catch (e: Exception) {
            android.util.Log.e("AddCategoryFragment", "Error: ${e.message}", e)
            Toast.makeText(requireContext(), "Lỗi khởi tạo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        binding.toolbar.setBackgroundColor(colorInt)
        binding.toolbar.setTitleTextColor(ThemeManager.getOnThemeColor())
        binding.btnSave.setBackgroundColor(colorInt)
    }

    private fun setupTypeButtons() {
        binding.btnExpense.setOnClickListener {
            selectedType = "expense"
            updateTypeButtonsStyle()
            updateEmojiListForType()
            updateColorListForType()
        }

        binding.btnIncome.setOnClickListener {
            selectedType = "income"
            updateTypeButtonsStyle()
            updateEmojiListForType()
            updateColorListForType()
        }

        selectedType = "expense"
        updateTypeButtonsStyle()
    }

    private fun updateTypeButtonsStyle() {
        val themeColor = ThemeManager.getThemeColorInt()

        if (selectedType == "expense") {
            binding.btnExpense.setBackgroundColor(themeColor)
            binding.btnExpense.setTextColor(ColorStateList.valueOf(Color.WHITE))
            binding.btnIncome.setBackgroundColor(Color.TRANSPARENT)
            binding.btnIncome.setTextColor(ColorStateList.valueOf(themeColor))
            binding.btnIncome.setStrokeColor(ColorStateList.valueOf(themeColor))
        } else {
            binding.btnIncome.setBackgroundColor(themeColor)
            binding.btnIncome.setTextColor(ColorStateList.valueOf(Color.WHITE))
            binding.btnExpense.setBackgroundColor(Color.TRANSPARENT)
            binding.btnExpense.setTextColor(ColorStateList.valueOf(themeColor))
            binding.btnExpense.setStrokeColor(ColorStateList.valueOf(themeColor))
        }
    }

    private fun setupEmojiRecyclerView() {
        val emojis = if (selectedType == "expense") EmojiCategory.EXPENSE else EmojiCategory.INCOME
        emojiAdapter = EmojiSelectAdapter(emojis) { emoji ->
            selectedEmoji = emoji
        }
        binding.rvEmojis.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvEmojis.adapter = emojiAdapter
    }

    private fun updateEmojiListForType() {
        val emojis = if (selectedType == "expense") EmojiCategory.EXPENSE else EmojiCategory.INCOME
        emojiAdapter.updateList(emojis)
    }

    private fun setupColorRecyclerView() {
        val colors = getAvailableColors()
        colorAdapter = ColorSelectAdapter(colors) { colorHex ->
            selectedColor = colorHex
        }
        binding.rvColors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorAdapter
    }

    private fun updateColorListForType() {
        colorAdapter.updateList(getAvailableColors())
    }

    private fun getAvailableColors(): List<String> {
        return listOf(
            "#FF8C00", "#2196F3", "#E91E63", "#FFC107",
            "#9C27B0", "#F44336", "#3F51B5", "#FF5722",
            "#00BCD4", "#4CAF50", "#607D8B", "#22CC00"
        )
    }

    private fun setupActionButtons() {
        binding.btnCancel.setOnClickListener {
            parentFragment?.childFragmentManager?.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveCategory()
        }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
            return
        }

        SessionManager.init(requireContext())
        val userId = SessionManager.getCurrentUserId()
        val actualUserId = if (userId == -1L) 1L else userId

        val entity = CategoryEntity(
            userId = actualUserId,
            name = name,
            iconEmoji = selectedEmoji,
            type = selectedType,
            color = selectedColor,
            isDefault = 0  // Luôn là 0 khi người dùng tạo
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                viewModel.addCategory(entity)
                Toast.makeText(requireContext(), "Thêm danh mục thành công", Toast.LENGTH_SHORT).show()
                parentFragment?.childFragmentManager?.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}