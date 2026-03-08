package me.jakev.nexuscore.data.api

import me.jakev.nexuscore.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface NexusApi {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthUser

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
    @POST("assets/import")
    suspend fun importCsv(@Part file: MultipartBody.Part): CsvImportResult

    @GET("assets/sample-csv")
    suspend fun downloadSampleCsv(): okhttp3.ResponseBody

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

    // ── Audit logs ────────────────────────────────────────────────────────
    @GET("audit-logs")
    suspend fun getAuditLogs(@Query("page") page: Int = 1): PaginatedAuditLogs
}
