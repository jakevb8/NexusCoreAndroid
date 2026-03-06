package com.nexuscore.android.ui.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscore.android.data.api.NexusApi
import com.nexuscore.android.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamUiState(
    val members: List<TeamMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isManager: Boolean = false,
    val currentUserId: String? = null,
    val inviteLink: String? = null
)

@HiltViewModel
class TeamViewModel @Inject constructor(private val api: NexusApi) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val me = api.me()
                val members = api.getTeam()
                _uiState.update {
                    it.copy(
                        members = members,
                        isLoading = false,
                        isManager = me.role == Role.ORG_MANAGER || me.role == Role.SUPERADMIN,
                        currentUserId = me.id
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun invite(email: String) {
        viewModelScope.launch {
            try {
                val response = api.inviteMember(InviteRequest(email))
                _uiState.update {
                    it.copy(
                        successMessage = "Invite sent to $email",
                        inviteLink = response.inviteLink
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeMember(id: String) {
        viewModelScope.launch {
            try {
                api.removeMember(id)
                _uiState.update { it.copy(successMessage = "Member removed") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateRole(id: String, role: Role) {
        viewModelScope.launch {
            try {
                api.updateMemberRole(id, UpdateRoleRequest(role))
                _uiState.update { it.copy(successMessage = "Role updated") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null, inviteLink = null) }
}
