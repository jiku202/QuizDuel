package com.quizduel.android.data.repository

import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class SectionRepository(private val apiClient: ApiClient) {

    private val gson = Gson()

    suspend fun getSections(): NetworkResult<List<SectionDto>> {
        return safeApiCall { apiClient.getService().getSections() }
    }

    suspend fun createSection(request: CreateSectionRequest): NetworkResult<SectionDto> {
        return safeApiCall { apiClient.getService().createSection(request) }
    }

    suspend fun joinSection(joinCode: String): NetworkResult<JoinResponse> {
        return safeApiCall { apiClient.getService().joinSection(JoinSectionRequest(joinCode.trim())) }
    }

    suspend fun updateSection(id: Long, request: UpdateSectionRequest): NetworkResult<SectionDto> {
        return safeApiCall { apiClient.getService().updateSection(id, request) }
    }

    suspend fun deleteSection(id: Long): NetworkResult<Unit> {
        return safeApiCall { apiClient.getService().deleteSection(id) }
    }

    suspend fun getRoster(sectionId: Long): NetworkResult<List<RosterEntry>> {
        return safeApiCall { apiClient.getService().getRoster(sectionId) }
    }

    suspend fun addMembers(sectionId: Long, emails: List<String>): NetworkResult<AddMembersResponse> {
        return safeApiCall { apiClient.getService().addMembers(sectionId, AddMembersRequest(emails)) }
    }

    suspend fun removeMember(sectionId: Long, studentId: Long): NetworkResult<Unit> {
        return safeApiCall { apiClient.getService().removeMember(sectionId, studentId) }
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
