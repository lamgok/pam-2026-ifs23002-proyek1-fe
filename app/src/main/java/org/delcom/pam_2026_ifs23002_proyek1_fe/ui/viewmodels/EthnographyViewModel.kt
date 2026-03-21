package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.*
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service.IEthnographyRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface EthnographiesUIState {
    data class Success(val data: List<ResponseEthnographyData>) : EthnographiesUIState
    data class Error(val message: String) : EthnographiesUIState
    object Loading : EthnographiesUIState
}

sealed interface EthnographyUIState {
    data class Success(val data: ResponseEthnographyData) : EthnographyUIState
    data class Error(val message: String) : EthnographyUIState
    object Loading : EthnographyUIState
}

sealed interface EthnographyActionUIState {
    data class Success(val message: String, val id: String? = null) : EthnographyActionUIState
    data class Error(val message: String) : EthnographyActionUIState
    object Loading : EthnographyActionUIState
    object Idle : EthnographyActionUIState
}

data class UIStateEthnography(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val ethnographies: EthnographiesUIState = EthnographiesUIState.Loading,
    var ethnography: EthnographyUIState = EthnographyUIState.Loading,
    var ethnographyAdd: EthnographyActionUIState = EthnographyActionUIState.Idle,
    var ethnographyChange: EthnographyActionUIState = EthnographyActionUIState.Idle,
    var ethnographyDelete: EthnographyActionUIState = EthnographyActionUIState.Idle,
    var ethnographyChangeImage: EthnographyActionUIState = EthnographyActionUIState.Idle,
    var profilePhotoChange: EthnographyActionUIState = EthnographyActionUIState.Idle
)

@HiltViewModel
@Keep
class EthnographyViewModel @Inject constructor(
    private val repository: IEthnographyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateEthnography())
    val uiState = _uiState.asStateFlow()

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val response = runCatching { repository.getUserMe(authToken) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") ProfileUIState.Success(it.data!!.user)
                        else ProfileUIState.Error(it.message)
                    },
                    onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(profile = it) }
            }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profilePhotoChange = EthnographyActionUIState.Loading) }
            val response = runCatching { repository.putUserMePhoto(authToken, file) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            getProfile(authToken)
                            EthnographyActionUIState.Success(it.message)
                        }
                        else EthnographyActionUIState.Error(it.message)
                    },
                    onFailure = { EthnographyActionUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(profilePhotoChange = it) }
            }
        }
    }

    fun getAllEthnographies(authToken: String, search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnographies = EthnographiesUIState.Loading) }
            val response = runCatching { repository.getEthnographies(authToken, search) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographiesUIState.Success(it.data?.ethnographies ?: emptyList())
                        else EthnographiesUIState.Error(it.message)
                    },
                    onFailure = { EthnographiesUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographies = it) }
            }
        }
    }

    fun postEthnography(authToken: String, request: EthnographyRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnographyAdd = EthnographyActionUIState.Loading) }
            val response = runCatching { repository.postEthnography(authToken, request) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographyActionUIState.Success(it.message, it.data?.ethnographyId)
                        else EthnographyActionUIState.Error(it.message)
                    },
                    onFailure = { EthnographyActionUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographyAdd = it) }
            }
        }
    }

    fun getEthnographyById(authToken: String, id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnography = EthnographyUIState.Loading) }
            val response = runCatching { repository.getEthnographyById(authToken, id) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographyUIState.Success(it.data!!.ethnography)
                        else EthnographyUIState.Error(it.message)
                    },
                    onFailure = { EthnographyUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnography = it) }
            }
        }
    }

    fun putEthnography(authToken: String, id: String, request: EthnographyRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnographyChange = EthnographyActionUIState.Loading) }
            val response = runCatching { repository.putEthnography(authToken, id, request) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographyActionUIState.Success(it.message)
                        else EthnographyActionUIState.Error(it.message)
                    },
                    onFailure = { EthnographyActionUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographyChange = it) }
            }
        }
    }

    fun putEthnographyImage(authToken: String, id: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnographyChangeImage = EthnographyActionUIState.Loading) }
            val response = runCatching { repository.putEthnographyImage(authToken, id, file) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographyActionUIState.Success(it.message)
                        else EthnographyActionUIState.Error(it.message)
                    },
                    onFailure = { EthnographyActionUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographyChangeImage = it) }
            }
        }
    }

    fun deleteEthnography(authToken: String, id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(ethnographyDelete = EthnographyActionUIState.Loading) }
            val response = runCatching { repository.deleteEthnography(authToken, id) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") EthnographyActionUIState.Success(it.message)
                        else EthnographyActionUIState.Error(it.message)
                    },
                    onFailure = { EthnographyActionUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographyDelete = it) }
            }
        }
    }

    fun resetActionStates() {
        _uiState.update {
            it.copy(
                ethnographyAdd = EthnographyActionUIState.Idle,
                ethnographyChange = EthnographyActionUIState.Idle,
                ethnographyDelete = EthnographyActionUIState.Idle,
                ethnographyChangeImage = EthnographyActionUIState.Idle,
                profilePhotoChange = EthnographyActionUIState.Idle
            )
        }
    }
}