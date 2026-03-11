package me.jakev.nexuscore.data.api

import me.jakev.nexuscore.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface NexusApi {

    // ── Auth ──────────────────────────────────────────────────────────────
    // Returns Response<Unit> because the register endpoint returns a bare User
    // object (no nested organization), which is incompatible with AuthUser.
    // The response body is not needed — onDone() triggers a fresh /auth/me fetch.
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @GET("auth/me")
    suspend fun me(): AuthUser

    @DELETE("auth/me")
    suspend fun deleteAccount()

    // ── Assets ────────────────────────────────────────────────────────────
    @GET("assets")
    suspend fun getAssets(
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null
    ): PaginatedAssets

    @POST("assets")
    suspend fun createAsset(@Body body: CreateAssetRequest): Asset

    @PUT("assets/{id}")
    suspend fun updateAsset(@Path("id") id: String, @Body body: UpdateAssetRequest): Asset

    @DELETE("assets/{id}")
    suspend fun deleteAsset(@Path("id") id: String)

    @Multipart
    @POST("assets/import/csv")
    suspend fun importCsv(@Part file: MultipartBody.Part): CsvImportResult

    // ── Team ──────────────────────────────────────────────────────────────
    @GET("users")
    suspend fun getTeam(): List<TeamMember>

    @POST("users/invite")
    suspend fun inviteMember(@Body body: InviteRequest): InviteResponse

    @DELETE("users/{id}")
    suspend fun removeMember(@Path("id") id: String)

    @PATCH("users/{id}/role")
    suspend fun updateMemberRole(@Path("id") id: String, @Body body: UpdateRoleRequest): TeamMember

    // ── Reports ───────────────────────────────────────────────────────────
    // .NET backend: GET /reports
    @GET("reports")
    suspend fun getDotNetReports(): DotNetReportsResponse

    // JS backend: GET /reports/stats
    @GET("reports/stats")
    suspend fun getJsReports(): JsReportsResponse

    // ── Events ────────────────────────────────────────────────────────────
    @GET("events")
    suspend fun getEvents(@Query("page") page: Int = 1): PaginatedEvents

    // ── Audit logs ────────────────────────────────────────────────────────
    @GET("audit-logs")
    suspend fun getAuditLogs(@Query("page") page: Int = 1): PaginatedAuditLogs
}
