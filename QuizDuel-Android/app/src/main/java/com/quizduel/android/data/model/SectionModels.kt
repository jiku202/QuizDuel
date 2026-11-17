package com.quizduel.android.data.model

data class CreateSectionRequest(
    val name: String,
    val gradeLevel: String = "Grade 3",
    val subject: String = "Araling Panlipunan",
    val memberEmails: List<String>? = null
)

data class UpdateSectionRequest(
    val name: String? = null,
    val gradeLevel: String? = null,
    val subject: String? = null,
    val battleTime: String? = null,
    val weekendBattles: Boolean? = null
)

data class AddMembersRequest(
    val emails: List<String>
)

data class AddMembersResponse(
    val added: List<RosterEntry> = emptyList(),
    val notFound: List<String> = emptyList(),
    val alreadyIn: List<String> = emptyList()
)

data class JoinSectionRequest(
    val joinCode: String
)

data class SectionDto(
    val id: Long,
    val name: String,
    val gradeLevel: String = "Grade 3",
    val subject: String = "Araling Panlipunan",
    val joinCode: String,
    val battleTime: String = "15:00",
    val weekendBattles: Boolean = false,
    val studentCount: Int? = null
)

data class JoinResponse(
    val joined: Boolean,
    val section: SectionDto
)

data class RosterEntry(
    val id: Long,
    val name: String,
    val email: String,
    val points: Int = 0,
    val streak: Int = 0,
    val rank: String? = null
)

data class BadgeDto(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val pointsThreshold: Int? = null
)
