package me.jakev.nexuscore.data.model

import com.squareup.moshi.JsonClass

// ── Enums ─────────────────────────────────────────────────────────────────

enum class AssetStatus { AVAILABLE, IN_USE, MAINTENANCE, RETIRED }
enum class Role { SUPERADMIN, ORG_MANAGER, ASSET_MANAGER, VIEWER }
enum class OrgStatus { PENDING, ACTIVE, REJECTED }

// ── Auth ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val organizationName: String,
    val organizationSlug: String,
    val displayName: String?
)

@JsonClass(generateAdapter = true)
data class AuthUserOrg(
    val id: String,
    val name: String,
    val status: OrgStatus
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: Role,
    val organizationId: String?,
    val organization: AuthUserOrg?
)

// ── Assets ────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class Asset(
    val id: String,
    val name: String,
    val sku: String,
    val description: String?,
    val status: AssetStatus,
    val assignedTo: String?,
    val organizationId: String,
    val createdAt: String,
    val updatedAt: String?
)

// Nested meta object returned by JS backend: {"data":[...],"meta":{"total":8,"page":1,"perPage":20}}
@JsonClass(generateAdapter = true)
data class PaginatedMeta(
    val total: Int?,
    val page: Int?,
    val perPage: Int?
)

// Handles both backends:
//   JS:    { data, meta: { total, page, perPage } }
//   .NET:  { data, total, page, perPage }
@JsonClass(generateAdapter = true)
data class PaginatedAssets(
    val data: List<Asset>,
    // .NET flat fields
    val total: Int? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    // JS nested meta
    val meta: PaginatedMeta? = null
) {
    fun resolvedTotal(): Int = meta?.total ?: total ?: 0
    fun resolvedPage(): Int = meta?.page ?: page ?: 1
}

@JsonClass(generateAdapter = true)
data class CreateAssetRequest(
    val name: String,
    val sku: String,
    val description: String?,
    val status: AssetStatus,
    val assignedTo: String?
)

@JsonClass(generateAdapter = true)
data class UpdateAssetRequest(
    val name: String,
    val sku: String,
    val description: String?,
    val status: AssetStatus,
    val assignedTo: String?
)

@JsonClass(generateAdapter = true)
data class CsvImportResult(
    val created: Int,
    val skipped: Int,
    val limitReached: Boolean,
    val errors: List<String>
)

// ── Team ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TeamMember(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: Role,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class InviteRequest(val email: String, val role: Role)

@JsonClass(generateAdapter = true)
data class InviteResponse(val inviteLink: String?)

@JsonClass(generateAdapter = true)
data class UpdateRoleRequest(val role: Role)

// ── Reports ───────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class StatusBreakdownItem(val status: AssetStatus, val count: Int)

// .NET backend response from GET /reports:
// { totalAssets, utilizationRate, assetsByStatus: [{status, count}], ... }
@JsonClass(generateAdapter = true)
data class DotNetReportsResponse(
    val totalAssets: Int,
    val utilizationRate: Double,
    val assetsByStatus: List<StatusBreakdownItem>
)

// JS backend response from GET /reports/stats:
// { totalAssets, utilizationRate, byStatus: { AVAILABLE: n, IN_USE: n, ... }, totalUsers }
@JsonClass(generateAdapter = true)
data class JsReportsResponse(
    val totalAssets: Int,
    val utilizationRate: Double,
    val byStatus: Map<String, Int>
)

// Unified model used by the UI
data class ReportsData(
    val totalAssets: Int,
    val utilizationRate: Double,
    val byStatus: List<StatusBreakdownItem>
)

fun DotNetReportsResponse.toReportsData() = ReportsData(
    totalAssets = totalAssets,
    utilizationRate = utilizationRate,
    byStatus = assetsByStatus
)

fun JsReportsResponse.toReportsData() = ReportsData(
    totalAssets = totalAssets,
    utilizationRate = utilizationRate,
    byStatus = byStatus.mapNotNull { (key, count) ->
        runCatching { StatusBreakdownItem(AssetStatus.valueOf(key), count) }.getOrNull()
    }
)

// ── Events ────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class KafkaEvent(
    val id: String,
    val organizationId: String,
    val assetId: String?,
    val assetName: String?,
    val previousStatus: String?,
    val newStatus: String?,
    val actorId: String?,
    val occurredAt: String,
    val createdAt: String
)

// Handles both backends (same dual-shape as PaginatedAssets)
@JsonClass(generateAdapter = true)
data class PaginatedEvents(
    val data: List<KafkaEvent>,
    // .NET flat fields
    val total: Int? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    // JS nested meta
    val meta: PaginatedMeta? = null
) {
    fun resolvedTotal(): Int = meta?.total ?: total ?: 0
    fun resolvedPage(): Int = meta?.page ?: page ?: 1
}

// ── Audit log ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class AuditLog(
    val id: String,
    val action: String,
    val actorId: String?,
    val assetId: String?,
    val createdAt: String
)

// Handles both backends (same dual-shape as PaginatedAssets)
@JsonClass(generateAdapter = true)
data class PaginatedAuditLogs(
    val data: List<AuditLog>,
    // .NET flat fields
    val total: Int? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    // JS nested meta
    val meta: PaginatedMeta? = null
) {
    fun resolvedTotal(): Int = meta?.total ?: total ?: 0
}
