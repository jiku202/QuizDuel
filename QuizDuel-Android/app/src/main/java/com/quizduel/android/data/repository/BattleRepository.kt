package com.quizduel.android.data.repository

import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class BattleRepository(private val apiClient: ApiClient) {

    private val gson = Gson()

    suspend fun getTodaysBattles(sectionId: String? = null): NetworkResult<List<BattleRow>> {
        return safeApiCall { apiClient.getService().getTodaysBattles(sectionId) }
    }

    suspend fun getBattleLog(sectionId: String? = null, limit: Int? = null): NetworkResult<List<BattleRow>> {
        return safeApiCall { apiClient.getService().getBattleLog(sectionId, limit) }
    }

    suspend fun generateBattles(sectionId: Long, battleTime: String? = null, weekendBattles: Boolean? = null): NetworkResult<GenerateResult> {
        return safeApiCall { apiClient.getService().generateBattles(GenerateRequest(sectionId, battleTime, weekendBattles)) }
    }

    suspend fun getMyTodaysBattles(): NetworkResult<List<BattleRow>> {
        return safeApiCall { apiClient.getService().getMyTodaysBattles() }
    }

    suspend fun getBattleDetail(battleId: Long): NetworkResult<BattleDetail> {
        return safeApiCall { apiClient.getService().getBattleDetail(battleId) }
    }

    suspend fun submitAnswers(battleId: Long, answers: List<AnswerItem>): NetworkResult<SubmitResponse> {
        return safeApiCall { apiClient.getService().submitAnswers(battleId, SubmitAnswersRequest(answers)) }
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
