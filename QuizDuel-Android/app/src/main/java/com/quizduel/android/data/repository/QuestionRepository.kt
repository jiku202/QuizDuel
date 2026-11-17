package com.quizduel.android.data.repository

import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class QuestionRepository(private val apiClient: ApiClient) {

    private val gson = Gson()

    suspend fun listTopics(sectionId: String? = null): NetworkResult<List<TopicDto>> {
        return safeApiCall { apiClient.getService().listTopics(sectionId) }
    }

    suspend fun createTopic(sectionId: Long, name: String): NetworkResult<TopicDto> {
        return safeApiCall { apiClient.getService().createTopic(TopicRequest(sectionId, name)) }
    }

    suspend fun renameTopic(topicId: Long, name: String): NetworkResult<TopicDto> {
        return safeApiCall { apiClient.getService().renameTopic(topicId, TopicRequest(name = name)) }
    }

    suspend fun copyTopic(topicId: Long, targetSectionId: Long, copyQuestions: Boolean = true): NetworkResult<TopicDto> {
        return safeApiCall { apiClient.getService().copyTopic(topicId, CopyTopicRequest(targetSectionId, copyQuestions)) }
    }

    suspend fun deleteTopic(topicId: Long): NetworkResult<Unit> {
        return safeApiCall { apiClient.getService().deleteTopic(topicId) }
    }

    suspend fun getQuestions(sectionId: String? = null, topic: String? = null): NetworkResult<List<QuestionDto>> {
        return safeApiCall { apiClient.getService().getQuestions(sectionId, topic) }
    }

    suspend fun getQuestionTopics(sectionId: String? = null): NetworkResult<List<String>> {
        return safeApiCall { apiClient.getService().getQuestionTopics(sectionId) }
    }

    suspend fun createQuestion(request: QuestionRequest): NetworkResult<QuestionDto> {
        return safeApiCall { apiClient.getService().createQuestion(request) }
    }

    suspend fun updateQuestion(id: Long, request: QuestionRequest): NetworkResult<QuestionDto> {
        return safeApiCall { apiClient.getService().updateQuestion(id, request) }
    }

    suspend fun deleteQuestion(id: Long): NetworkResult<Unit> {
        return safeApiCall { apiClient.getService().deleteQuestion(id) }
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
