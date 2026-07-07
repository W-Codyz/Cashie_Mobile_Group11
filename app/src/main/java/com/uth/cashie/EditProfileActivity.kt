package com.uth.cashie

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uth.cashie.database.CashieDatabase
import com.uth.cashie.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import com.uth.cashie.database.SessionManager

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val db     by lazy { CashieDatabase.getInstance(this) }
    private val userId by lazy { SessionManager.getCurrentUserId() }

    private var selectedCurrency = "VND"
    private var newAvatarPath: String? = null   // đường dẫn ảnh mới nếu user đổi

    // Launcher mở Gallery
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleImagePicked(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyTheme()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnChangePhoto.setOnClickListener { openGallery() }
        binding.btnSave.setOnClickListener { saveProfile() }

        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    // ── Áp dụng màu chủ đề ───────────────────────────────────────────────────
    private fun applyTheme() {
        val colorInt = ThemeManager.getThemeColorInt()
        val colorList = android.content.res.ColorStateList.valueOf(colorInt)

        val avatarBg = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(colorInt)
        }
        binding.ivEditAvatar.background = avatarBg

        // Avatar icon tint — chỉ áp dụng khi chưa có ảnh thật
        if (binding.ivEditAvatar.imageTintList != null) {
            binding.ivEditAvatar.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            binding.ivEditAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            binding.ivEditAvatar.setPadding(20, 20, 20, 20)
        }

        ThemeManager.applyThemeToWindow(this, binding.appBarLayout,
            binding.tvToolbarTitle, binding.btnBack)
        ThemeManager.applyToButton(binding.btnChangePhoto)
        ThemeManager.applyToButton(binding.btnSave)

        // TextInputLayout stroke + hint
        ThemeManager.applyToTextInput(binding.tilFullName)
        ThemeManager.applyToTextInput(binding.tilEmail)

        // RadioButton tint
        binding.radioEditVnd.buttonTintList = colorList
        binding.radioEditUsd.buttonTintList = colorList
    }

    // ── Load dữ liệu hiện tại ─────────────────────────────────────────────────
    private fun loadUserData() {
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) }
            user ?: return@launch

            binding.etFullName.setText(user.fullName)
            binding.etUsername.setText(user.username)
            binding.etEmail.setText(user.email ?: "")

            selectedCurrency = user.currency
            updateCurrencyUi()

            // Avatar
            if (!user.avatarPath.isNullOrBlank()) {
                val file = File(user.avatarPath)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    binding.ivEditAvatar.setImageBitmap(bmp)
                    binding.ivEditAvatar.imageTintList = null
                    binding.ivEditAvatar.scaleType =
                        android.widget.ImageView.ScaleType.CENTER_CROP
                    binding.ivEditAvatar.setPadding(0, 0, 0, 0)
                }
            } else {
                // icon mặc định: tô trắng trên nền xanh
                binding.ivEditAvatar.imageTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.WHITE
                    )
                binding.ivEditAvatar.scaleType =
                    android.widget.ImageView.ScaleType.CENTER_INSIDE
                binding.ivEditAvatar.setPadding(20, 20, 20, 20)
            }
        }

        // Toggle currency
        binding.cardEditVnd.setOnClickListener { selectedCurrency = "VND"; updateCurrencyUi() }
        binding.radioEditVnd.setOnClickListener { selectedCurrency = "VND"; updateCurrencyUi() }
        binding.cardEditUsd.setOnClickListener  { selectedCurrency = "USD"; updateCurrencyUi() }
        binding.radioEditUsd.setOnClickListener { selectedCurrency = "USD"; updateCurrencyUi() }
    }

    private fun updateCurrencyUi() {
        if (selectedCurrency == "VND") {
            binding.cardEditVnd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.cardEditUsd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.radioEditVnd.isChecked = true
            binding.radioEditUsd.isChecked = false
        } else {
            binding.cardEditVnd.setBackgroundResource(R.drawable.bg_currency_card_default)
            binding.cardEditUsd.setBackgroundResource(R.drawable.bg_currency_card_selected)
            binding.radioEditVnd.isChecked = false
            binding.radioEditUsd.isChecked = true
        }
    }

    // ── Gallery ───────────────────────────────────────────────────────────────
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    /**
     * Copy ảnh từ URI (content://) vào thư mục internal storage của app.
     * Lý do: content URI có thể bị thu hồi sau khi app restart,
     * dùng file nội bộ đảm bảo luôn đọc được.
     */
    private fun handleImagePicked(uri: Uri) {
        lifecycleScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                    val avatarDir = File(filesDir, "avatars")
                    avatarDir.mkdirs()
                    val file = File(avatarDir, "avatar_$userId.jpg")
                    FileOutputStream(file).use { out -> inputStream.copyTo(out) }
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            savedPath?.let { path ->
                newAvatarPath = path
                val bmp = BitmapFactory.decodeFile(path)
                binding.ivEditAvatar.setImageBitmap(bmp)
                binding.ivEditAvatar.imageTintList = null
                binding.ivEditAvatar.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.ivEditAvatar.setPadding(0, 0, 0, 0)
            }
        }
    }

    // ── Lưu ──────────────────────────────────────────────────────────────────
    private fun saveProfile() {
        val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
        val email = binding.tilEmail.editText!!.text.toString().trim()


        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_full_name_empty)
            return
        }
        binding.tilFullName.error = null

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getById(userId) }
            user ?: return@launch

            withContext(Dispatchers.IO) {
                // Cập nhật tên, email, currency
                db.userDao().update(
                    user.copy(
                        fullName = fullName,
                        email    = email,
                        currency = selectedCurrency
                    )
                )
                // Cập nhật avatar nếu user đổi ảnh
                newAvatarPath?.let { path ->
                    db.userDao().updateAvatar(userId, path)
                }
            }
            // Lưu tiền tệ vào SessionManager
            SessionManager.setCurrency(selectedCurrency)

            Toast.makeText(
                this@EditProfileActivity,
                getString(R.string.toast_saved),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
