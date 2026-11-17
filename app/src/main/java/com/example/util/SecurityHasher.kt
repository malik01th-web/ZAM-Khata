package com.example.util

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityHasher {

    private const val SALT_BYTES = 16
    private const val HASH_BITS = 256
    private const val ITERATIONS = 100_000
    private const val PREFIX_SHA256 = "pbkdf2_sha256"
    private const val PREFIX_SHA1 = "pbkdf2_sha1"

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val secureRandom = SecureRandom()

    fun normalizeEmail(raw: String): String = raw.trim().lowercase()

    fun isValidEmail(raw: String): Boolean {
        val cleaned = normalizeEmail(raw)
        return cleaned.isNotEmpty() && emailRegex.matches(cleaned)
    }

    fun isValidMpin(raw: String): Boolean = raw.length == 4 && raw.all { it.isDigit() }

    fun hashSecret(secret: String): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        val (algoName, prefix) = selectAlgorithm()
        val derived = deriveKey(secret.toCharArray(), salt, ITERATIONS, algoName)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(derived, Base64.NO_WRAP)
        return "$prefix$$ITERATIONS$$saltB64$$hashB64"
    }

    fun verifySecret(secret: String, storedHash: String): Boolean {
        val parts = storedHash.split("$")
        if (parts.size != 4) return false
        val algoName = when (parts[0]) {
            PREFIX_SHA256 -> "PBKDF2WithHmacSHA256"
            PREFIX_SHA1 -> "PBKDF2WithHmacSHA1"
            else -> return false
        }
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = try {
            Base64.decode(parts[2], Base64.NO_WRAP)
        } catch (_: IllegalArgumentException) {
            return false
        }
        val expectedHash = try {
            Base64.decode(parts[3], Base64.NO_WRAP)
        } catch (_: IllegalArgumentException) {
            return false
        }
        val actualHash = try {
            deriveKey(secret.toCharArray(), salt, iterations, algoName)
        } catch (_: Exception) {
            return false
        }
        return MessageDigest.isEqual(expectedHash, actualHash)
    }

    private fun selectAlgorithm(): Pair<String, String> {
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            "PBKDF2WithHmacSHA256" to PREFIX_SHA256
        } catch (_: NoSuchAlgorithmException) {
            "PBKDF2WithHmacSHA1" to PREFIX_SHA1
        }
    }

    private fun deriveKey(
        chars: CharArray,
        salt: ByteArray,
        iterations: Int,
        algorithm: String
    ): ByteArray {
        val spec = PBEKeySpec(chars, salt, iterations, HASH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(algorithm)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
