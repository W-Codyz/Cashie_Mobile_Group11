package com.uth.cashie.util

import java.security.MessageDigest

object PasswordUtils {
    fun verify(password: String, hash: String): Boolean {
        return this.hash(password) == hash
    }

    fun hash(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }
}