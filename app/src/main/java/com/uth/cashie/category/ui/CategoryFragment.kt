package com.uth.cashie.category.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.uth.cashie.category.adapter.CategoryAdapter
import com.uth.cashie.databinding.FragmentCategoryBinding
import com.uth.cashie.category.ui.CategoryViewModel   // ✅ Import ViewModel từ package ui
import com.google.android.material.tabs.TabLayout

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()
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
        applyTheme()
        setupRecyclerView()
        setupTabLayout()
        observeData()
    }

    private fun applyTheme() {
        val colorInt = com.uth.cashie.ThemeManager.getThemeColorInt()
        // Toolbar background
        binding.toolbar.setBackgroundColor(colorInt)
        val onColor = com.uth.cashie.ThemeManager.getOnThemeColor()
        binding.toolbar.setTitleTextColor(onColor)
        // TabLayout selected indicator + text
        binding.tabLayout.setSelectedTabIndicatorColor(colorInt)
        binding.tabLayout.setTabTextColors(
            android.graphics.Color.parseColor("#888888"),
            colorInt
        )
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { category ->
            // Xử lý click
        }
        binding.rvCategory.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvCategory.adapter = adapter
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val type = if (tab?.position == 0) "expense" else "income"
                viewModel.switchTab(type)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeData() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
        }
    }
}