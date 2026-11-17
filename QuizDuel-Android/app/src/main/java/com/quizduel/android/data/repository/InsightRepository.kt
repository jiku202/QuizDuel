package com.quizduel.android.data.repository

import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class InsightRepository(private val apiClient: ApiClient) {

    private val gson = Gson()

    suspend fun getDashboard(sectionId: String? = null): NetworkResult<DashboardResponse> {
        return safeApiCall { apiClient.getService().getDashboard(sectionId) }
    }

    suspend fun getAnalytics(sectionId: String? = null): NetworkResult<AnalyticsResponse> {
        return safeApiCall { apiClient.getService().getAnalytics(sectionId) }
    }

    suspend fun getLeaderboard(sectionId: String? = null, sectionIds: String? = null): NetworkResult<List<LeaderboardEntry>> {
        return safeApiCall { apiClient.getService().getLeaderboard(sectionId, sectionIds) }
    }

    suspend fun getBadges(): NetworkResult<List<BadgeDto>> {
        return safeApiCall { apiClient.getService().getBadges() }
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
            NetworkResult.Error(e.localizedMessage ?: "Network error")
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
