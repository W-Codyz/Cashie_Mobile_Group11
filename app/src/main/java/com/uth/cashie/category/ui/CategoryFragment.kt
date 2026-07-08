package com.uth.cashie.category.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.uth.cashie.NavScreen
import com.uth.cashie.R
import com.uth.cashie.ThemeManager
import com.uth.cashie.category.adapter.CategoryAdapter
import com.uth.cashie.category.data.CategoryRepository
import com.uth.cashie.databinding.FragmentCategoryBinding
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.database.SessionManager
import com.uth.cashie.showNavMenu
import com.google.android.material.tabs.TabLayout

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sử dụng btnMenu thay vì toolbar.setNavigationOnClickListener
        binding.btnMenu.setOnClickListener {
            (requireActivity() as AppCompatActivity).showNavMenu(NavScreen.CATEGORIES)
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
            setupRecyclerView()
            setupTabLayout()
            setupFAB()
            observeData()

            childFragmentManager.addOnBackStackChangedListener {
                if (childFragmentManager.backStackEntryCount == 0) {
                    showMainContent()
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("CategoryFragment", "Error: ${e.message}", e)
        }
    }

    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        binding.tvTitle.setTextColor(colorInt)
        binding.tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT)
        binding.tabLayout.setTabTextColors(Color.parseColor("#777777"), Color.WHITE)
        binding.fabAddCategory.backgroundTintList =
            android.content.res.ColorStateList.valueOf(colorInt)

        // Tô màu nền card tab theo theme (màu container nhạt của theme hiện tại)
        binding.cardTabLayout.setCardBackgroundColor(ThemeManager.getSolidContainerColor())

        // Re-tô màu active tab theo theme hiện tại
        binding.tabLayout.post {
            val strip = binding.tabLayout.getChildAt(0) as? ViewGroup ?: return@post
            val selectedPos = binding.tabLayout.selectedTabPosition
            for (i in 0 until strip.childCount) {
                strip.getChildAt(i)?.background =
                    if (i == selectedPos) tabActiveBg() else tabInactiveBg()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun tabActiveBg(): GradientDrawable {
        val cornerRadius = 10f * resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ThemeManager.getThemeColorInt())
            this.cornerRadius = cornerRadius
        }
    }

    private fun tabInactiveBg(): GradientDrawable {
        val cornerRadius = 10f * resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.TRANSPARENT)
            this.cornerRadius = cornerRadius
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onItemClick = { category -> showCategoryDetail(category) },
            onItemLongClick = { category -> showDeleteDialog(category) }
        )
        binding.rvCategory.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvCategory.adapter = adapter
    }

    private fun hideBottomNav() {
        requireActivity().findViewById<View>(R.id.bubbleNav)?.visibility = View.GONE
    }

    private fun showBottomNav() {
        requireActivity().findViewById<View>(R.id.bubbleNav)?.visibility = View.VISIBLE
    }

    // ===================== MỞ TRANG CHI TIẾT =====================
    private fun showCategoryDetail(category: com.uth.cashie.category.model.Category) {
        hideBottomNav()
        binding.layoutHeader.visibility = View.GONE
        binding.cardTabLayout.visibility = View.GONE
        binding.rvCategory.visibility = View.GONE
        binding.fabAddCategory.visibility = View.GONE
        binding.containerAddCategory.visibility = View.VISIBLE

        val detailFragment = CategoryDetailFragment.newInstance(
            id = category.id,
            name = category.name,
            icon = category.icon,
            color = category.color,
            emoji = category.emoji,
            isDefault = category.isDefault
        )

        childFragmentManager.beginTransaction()
            .add(R.id.containerAddCategory, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // ===================== MỞ THÊM DANH MỤC =====================
    private fun showAddCategoryFragment() {
        hideBottomNav()
        binding.layoutHeader.visibility = View.GONE
        binding.cardTabLayout.visibility = View.GONE
        binding.rvCategory.visibility = View.GONE
        binding.fabAddCategory.visibility = View.GONE
        binding.containerAddCategory.visibility = View.VISIBLE

        childFragmentManager.beginTransaction()
            .add(R.id.containerAddCategory, AddCategoryFragment())
            .addToBackStack(null)
            .commit()
    }

    // ===================== HIỂN THỊ LẠI DANH SÁCH =====================
    private fun showMainContent() {
        showBottomNav()
        binding.layoutHeader.visibility = View.VISIBLE
        binding.cardTabLayout.visibility = View.VISIBLE
        binding.rvCategory.visibility = View.VISIBLE
        binding.fabAddCategory.visibility = View.VISIBLE
        binding.containerAddCategory.visibility = View.GONE

        val fragment = childFragmentManager.findFragmentById(R.id.containerAddCategory)
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    // ===================== XÓA DANH MỤC =====================
    private fun showDeleteDialog(category: com.uth.cashie.category.model.Category) {
        if (category.isDefault) {
            Toast.makeText(requireContext(), "Đây là danh mục mặc định, không thể xóa được", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Xóa danh mục")
            .setMessage("Bạn có chắc muốn xóa danh mục \"${category.name}\"?")
            .setPositiveButton("Xóa") { _, _ -> viewModel.deleteCategory(category.id.toLong()) }
            .setNegativeButton("Hủy", null)
            .show()
        val themeColor = ThemeManager.getThemeColorInt()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(themeColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(themeColor)
    }

    // ===================== TAB LAYOUT =====================
    private fun setupTabLayout() {
        // Áp dụng background động theo theme sau khi tabs được inflate
        binding.tabLayout.post {
            val strip = binding.tabLayout.getChildAt(0) as? ViewGroup ?: return@post
            for (i in 0 until strip.childCount) {
                strip.getChildAt(i)?.background = if (i == 0) tabActiveBg() else tabInactiveBg()
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val strip = binding.tabLayout.getChildAt(0) as? ViewGroup ?: return
                strip.getChildAt(tab?.position ?: 0)?.background = tabActiveBg()
                val type = if (tab?.position == 0) "expense" else "income"
                viewModel.switchTab(type)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val strip = binding.tabLayout.getChildAt(0) as? ViewGroup ?: return
                strip.getChildAt(tab?.position ?: 0)?.background = tabInactiveBg()
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // ===================== FAB =====================
    private fun setupFAB() {
        binding.fabAddCategory.setOnClickListener { showAddCategoryFragment() }
    }

    // ===================== QUAN SÁT DỮ LIỆU =====================
    private fun observeData() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
        }
    }
}
