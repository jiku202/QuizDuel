package com.quizduel.android.data.remote

import com.quizduel.android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface QuizDuelApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<MeResponse>

    // Sections
    @GET("sections")
    suspend fun getSections(): Response<List<SectionDto>>

    @POST("sections")
    suspend fun createSection(@Body request: CreateSectionRequest): Response<SectionDto>

    @POST("sections/join")
    suspend fun joinSection(@Body request: JoinSectionRequest): Response<JoinResponse>

    @PUT("sections/{id}")
    suspend fun updateSection(@Path("id") id: Long, @Body request: UpdateSectionRequest): Response<SectionDto>

    @DELETE("sections/{id}")
    suspend fun deleteSection(@Path("id") id: Long): Response<Unit>

    @GET("sections/{id}/roster")
    suspend fun getRoster(@Path("id") id: Long): Response<List<RosterEntry>>

    @POST("sections/{id}/roster")
    suspend fun addMembers(@Path("id") id: Long, @Body request: AddMembersRequest): Response<AddMembersResponse>

    @DELETE("sections/{id}/roster/{studentId}")
    suspend fun removeMember(@Path("id") id: Long, @Path("studentId") studentId: Long): Response<Unit>

    // Topics
    @GET("topics")
    suspend fun listTopics(@Query("sectionId") sectionId: String? = null): Response<List<TopicDto>>

    @POST("topics")
    suspend fun createTopic(@Body request: TopicRequest): Response<TopicDto>

    @PUT("topics/{id}")
    suspend fun renameTopic(@Path("id") id: Long, @Body request: TopicRequest): Response<TopicDto>

    @POST("topics/{id}/copy")
    suspend fun copyTopic(@Path("id") id: Long, @Body request: CopyTopicRequest): Response<TopicDto>

    @DELETE("topics/{id}")
    suspend fun deleteTopic(@Path("id") id: Long): Response<Unit>

    // Questions
    @GET("questions")
    suspend fun getQuestions(
        @Query("sectionId") sectionId: String? = null,
        @Query("topic") topic: String? = null
    ): Response<List<QuestionDto>>

    @GET("questions/topics")
    suspend fun getQuestionTopics(@Query("sectionId") sectionId: String? = null): Response<List<String>>

    @POST("questions")
    suspend fun createQuestion(@Body request: QuestionRequest): Response<QuestionDto>

    @PUT("questions/{id}")
    suspend fun updateQuestion(@Path("id") id: Long, @Body request: QuestionRequest): Response<QuestionDto>

    @DELETE("questions/{id}")
    suspend fun deleteQuestion(@Path("id") id: Long): Response<Unit>

    // Battles
    @GET("battles/today")
    suspend fun getTodaysBattles(@Query("sectionId") sectionId: String? = null): Response<List<BattleRow>>

    @GET("battles/log")
    suspend fun getBattleLog(
        @Query("sectionId") sectionId: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<BattleRow>>

    @POST("battles/generate")
    suspend fun generateBattles(@Body request: GenerateRequest): Response<GenerateResult>

    @GET("battles/me/today")
    suspend fun getMyTodaysBattles(): Response<List<BattleRow>>

    @GET("battles/{id}")
    suspend fun getBattleDetail(@Path("id") id: Long): Response<BattleDetail>

    @POST("battles/{id}/answers")
    suspend fun submitAnswers(
        @Path("id") id: Long,
        @Body request: SubmitAnswersRequest
    ): Response<SubmitResponse>

    // Insights
    @GET("dashboard")
    suspend fun getDashboard(@Query("sectionId") sectionId: String? = null): Response<DashboardResponse>

    @GET("analytics")
    suspend fun getAnalytics(@Query("sectionId") sectionId: String? = null): Response<AnalyticsResponse>

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("sectionId") sectionId: String? = null,
        @Query("sectionIds") sectionIds: String? = null
    ): Response<List<LeaderboardEntry>>

    @GET("badges")
    suspend fun getBadges(): Response<List<BadgeDto>>

    @GET("health")
    suspend fun getHealth(): Response<Map<String, String>>
}
