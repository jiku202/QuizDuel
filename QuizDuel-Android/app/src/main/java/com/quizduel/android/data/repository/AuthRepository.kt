package com.quizduel.android.data.repository

import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.core.security.TokenManager
import com.quizduel.android.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class AuthRepository(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun register(request: RegisterRequest): NetworkResult<AuthResponse> {
        return safeApiCall {
            val response = apiClient.getService().register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                tokenManager.saveUser(body.user)
            }
            response
        }
    }

    suspend fun login(request: LoginRequest): NetworkResult<AuthResponse> {
        return safeApiCall {
            val response = apiClient.getService().login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                tokenManager.saveUser(body.user)
            }
            response
        }
    }

    suspend fun getMe(): NetworkResult<UserDto> {
        return safeApiCall {
            val response = apiClient.getService().getMe()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                tokenManager.saveUser(user)
            }
            response
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(result.data.user)
                is NetworkResult.Error -> NetworkResult.Error(result.message, result.code)
                is NetworkResult.Loading -> NetworkResult.Loading
            }
        }
    }

    fun getSavedUser(): UserDto? = tokenManager.getUser()

    fun hasToken(): Boolean = !tokenManager.getToken().isNullOrBlank()

    fun logout() {
        tokenManager.clear()
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else if (response.isSuccessful && response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                NetworkResult.Success(Unit as T)
            } else {
                val errorMsg = extractErrorMessage(response)
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Network connection error")
        }
    }

    private fun <T> extractErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = gson.fromJson(errorBody, JsonObject::class.java)
                if (json.has("error")) {
                    return json.get("error").asString
                }
            }
            "Request failed with code ${response.code()}"
        } catch (e: Exception) {
            "Request failed with code ${response.code()}"
        }
    }
}
