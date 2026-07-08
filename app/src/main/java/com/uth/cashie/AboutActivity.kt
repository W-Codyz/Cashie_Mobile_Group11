package com.uth.cashie

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.uth.cashie.databinding.ActivityAboutBinding
import com.uth.cashie.databinding.ItemAboutMemberBinding

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    data class Member(
        val name: String,
        val studentId: String,
        val isLeader: Boolean,
        val initials: String,
        val colorHex: String
    )

    private val members = listOf(
        Member("Đặng Thành Đình Phát", "MSSV: 052205008286", true,  "ĐP", "#22CC00"),
        Member("Lê Duy Mạnh",          "MSSV: 052205008936", false, "LM", "#2196F3"),
        Member("Phan Dương Khang",      "MSSV: 052205008668", false, "DK", "#FF8C00"),
        Member("Nguyễn Tấn Đạt",       "MSSV: 052205009161", false, "TĐ", "#9C27B0"),
        Member("Huỳnh Phúc Đạt",       "MSSV: 052205001953", false, "PĐ", "#F44336"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        binding.btnAboutBack.setOnClickListener { finish() }
        populateMembers()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun applyTheme() {
        val colorInt     = ThemeManager.getThemeColorInt()
        val containerBg  = ThemeManager.getSolidContainerColor()

        // Toolbar: background + status bar + icon/title contrast color
        ThemeManager.applyThemeToWindow(
            this,
            binding.appBarAbout,
            binding.tvAboutTitle,
            binding.btnAboutBack
        )

        // Header gradient card
        ThemeManager.applyToGradientCard(binding.aboutHeader)

        // Badge text color (pill on gradient header)
        binding.tvAboutBadge.setTextColor(colorInt)

        // Section icon circles — both follow theme container color
        val circleTint = ColorStateList.valueOf(containerBg)
        binding.iconCircleCourse.backgroundTintList   = circleTint
        binding.iconCircleMembers.backgroundTintList  = circleTint

        // "Nhóm 11" value text
        binding.tvAboutGroupName.setTextColor(colorInt)
    }

    private fun populateMembers() {
        val inflater = LayoutInflater.from(this)
        members.forEach { member ->
            val itemBinding = ItemAboutMemberBinding.inflate(inflater, binding.membersContainer, false)

            // Avatar circle with per-member color (intentionally distinct, not theme-driven)
            itemBinding.tvMemberAvatar.text = member.initials
            runCatching {
                val color = Color.parseColor(member.colorHex)
                itemBinding.tvMemberAvatar.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            }

            itemBinding.tvMemberName.text      = member.name
            itemBinding.tvMemberStudentId.text = member.studentId

            if (member.isLeader) {
                itemBinding.tvLeaderBadge.visibility = View.VISIBLE
                itemBinding.tvLeaderBadge.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(ThemeManager.getThemeColorInt())
                    cornerRadius = 8f * resources.displayMetrics.density
                }
            }

            binding.membersContainer.addView(itemBinding.root)
        }
    }
}
