package com.quizduel.android.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.quizduel.android.data.model.UserDto
import com.google.gson.Gson

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "quizduel_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("quizduel_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_BASE_URL = "api_base_url"
        const val DEFAULT_EMULATOR_BASE_URL = "http://10.0.2.2:5000/api/"
        const val DEFAULT_LOCAL_BASE_URL = "http://127.0.0.1:5000/api/"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun saveUser(user: UserDto) {
        prefs.edit().putString(KEY_CURRENT_USER, gson.toJson(user)).apply()
    }

    fun getUser(): UserDto? {
        val json = prefs.getString(KEY_CURRENT_USER, null) ?: return null
        return try {
            gson.fromJson(json, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_CURRENT_USER)
            .apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_EMULATOR_BASE_URL) ?: DEFAULT_EMULATOR_BASE_URL
    }

    fun setBaseUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString(KEY_BASE_URL, normalized).apply()
    }
}
