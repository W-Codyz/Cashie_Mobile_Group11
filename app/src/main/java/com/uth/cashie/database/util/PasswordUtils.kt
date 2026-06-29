package com.uth.cashie.database.util

import java.security.MessageDigest

/**
 * Tiện ích hash mật khẩu bằng SHA-256.
 * Không bao giờ lưu mật khẩu dạng plaintext vào database.
 */
object PasswordUtils {

    /**
     * Hash mật khẩu bằng SHA-256.
     * @param password mật khẩu dạng plaintext
     * @return chuỗi hex 64 ký tự
     */
    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes  = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Kiểm tra mật khẩu khớp với hash đã lưu */
    fun verify(password: String, storedHash: String): Boolean =
        hash(password) == storedHash
}
