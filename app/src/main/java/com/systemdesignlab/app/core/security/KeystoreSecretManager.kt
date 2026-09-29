package com.systemdesignlab.app.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Enterprise-grade KeyStore-backed credential manager.
 * Encrypts API keys with AES-256-GCM using hardware-backed keys when available.
 * Never stores plain text or exposes raw keys in logs or DataStore.
 */
class KeystoreSecretManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SystemDesignLabMasterKey"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val SECRETS_DIR = "app_vault"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun getSecretFile(providerId: String): File {
        val dir = File(context.filesDir, SECRETS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val safeName = providerId.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        return File(dir, "$safeName.enc")
    }

    @Synchronized
    fun saveApiKey(providerId: String, apiKey: String): Boolean {
        return try {
            if (apiKey.isBlank()) {
                clearApiKey(providerId)
                return true
            }
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))

            // Format: Base64(IV):Base64(Ciphertext)
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ctB64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            val payload = "$ivB64:$ctB64"

            val file = getSecretFile(providerId)
            file.writeText(payload, Charsets.UTF_8)
            true
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun getApiKey(providerId: String): String? {
        return try {
            val file = getSecretFile(providerId)
            if (!file.exists()) return null
            val payload = file.readText(Charsets.UTF_8)
            val parts = payload.split(":")
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun hasApiKey(providerId: String): Boolean {
        val file = getSecretFile(providerId)
        return file.exists() && file.length() > 0
    }

    @Synchronized
    fun getMaskedKey(providerId: String): String? {
        val key = getApiKey(providerId) ?: return null
        return if (key.length <= 8) {
            "••••••••"
        } else {
            val last4 = key.takeLast(4)
            "••••••••••••$last4"
        }
    }

    @Synchronized
    fun clearApiKey(providerId: String): Boolean {
        return try {
            val file = getSecretFile(providerId)
            if (file.exists()) {
                // Secure overwrite before deletion
                file.writeBytes(ByteArray(file.length().toInt()))
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
