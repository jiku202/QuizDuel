package com.quizduel.android.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val school: String? = null,
    val role: String = "student"
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val school: String? = null,
    @SerializedName("totalPoints")
    val totalPoints: Int = 0,
    val streak: Int = 0
) {
    val isTeacher: Boolean get() = "teacher".equals(role, ignoreCase = true)
    val isStudent: Boolean get() = "student".equals(role, ignoreCase = true)
}

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class MeResponse(
    val user: UserDto
)
