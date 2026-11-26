package com.example.data.local

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 password hashing for Local Demo Mode accounts and local admin passcodes.
 * Production Firebase Cloud accounts authenticate exclusively via Google Sign-In (Credential Manager).
 */
object PasswordHasher {
  private const val ITERATIONS = 12_000
  private const val KEY_LENGTH_BITS = 256
  private val rng = SecureRandom()

  fun hashPassword(password: String): String {
    val salt = ByteArray(16)
    rng.nextBytes(salt)
    val hash = pbkdf2(password.toCharArray(), salt)
    val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
    val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
    return "$saltB64:$hashB64"
  }

  fun verifyPassword(password: String, stored: String): Boolean {
    val parts = stored.split(":")
    if (parts.size != 2) return false
    return try {
      val salt = Base64.decode(parts[0], Base64.NO_WRAP)
      val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)
      val actualHash = pbkdf2(password.toCharArray(), salt)
      MessageDigest.isEqual(expectedHash, actualHash)
    } catch (_: Exception) {
      false
    }
  }

  private fun pbkdf2(chars: CharArray, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(chars, salt, ITERATIONS, KEY_LENGTH_BITS)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    return factory.generateSecret(spec).encoded
  }
}
