package me.jakev.nexuscore.data.model

import com.squareup.moshi.JsonClass

// ── Enums ─────────────────────────────────────────────────────────────────

enum class AssetStatus { AVAILABLE, IN_USE, MAINTENANCE, RETIRED }
enum class Role { SUPERADMIN, ORG_MANAGER, ASSET_MANAGER, VIEWER }
enum class OrgStatus { PENDING, ACTIVE, REJECTED }

// ── Auth ──────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val firebaseToken: String,
    val orgName: String,
    val name: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class AuthUserOrg(
    val id: String,
    val status: OrgStatus
)

@JsonClass(generateAdapter = true)
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: Role,
    val organizationId: String,
    val organization: AuthUserOrg
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

@JsonClass(generateAdapter = true)
data class PaginatedAssets(
    val data: List<Asset>,
    val total: Int,
    val page: Int,
    val perPage: Int
)

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
    val name: String?,
    val role: Role,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class InviteRequest(val email: String)

@JsonClass(generateAdapter = true)
data class InviteResponse(val inviteLink: String?)

@JsonClass(generateAdapter = true)
data class UpdateRoleRequest(val role: Role)

// ── Reports ───────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class StatusBreakdownItem(val status: AssetStatus, val count: Int)

@JsonClass(generateAdapter = true)
data class ReportsData(
    val totalAssets: Int,
    val utilizationRate: Double,
    val byStatus: List<StatusBreakdownItem>
)

// ── Audit log ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class AuditLog(
    val id: String,
    val action: String,
    val actorId: String?,
    val assetId: String?,
    val createdAt: String
)

@JsonClass(generateAdapter = true)
data class PaginatedAuditLogs(
    val data: List<AuditLog>,
    val total: Int,
    val page: Int,
    val perPage: Int
)
