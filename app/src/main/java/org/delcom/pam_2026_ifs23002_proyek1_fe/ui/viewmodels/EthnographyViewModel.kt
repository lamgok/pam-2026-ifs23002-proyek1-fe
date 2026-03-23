package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels

import android.util.Log
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
    data class Success(val data: List<ResponseEthnographyData>, val isLastPage: Boolean = false) : EthnographiesUIState
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
    var profilePhotoChange: EthnographyActionUIState = EthnographyActionUIState.Idle,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val selectedRegion: String = "Semua"
)

@HiltViewModel
@Keep
class EthnographyViewModel @Inject constructor(
    private val repository: IEthnographyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateEthnography())
    val uiState = _uiState.asStateFlow()

    private var currentSearch: String? = null

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

    fun upsert(
        authToken: String,
        id: String?,
        tribeName: String,
        region: String,
        language: String,
        traditionalHouse: String,
        traditionalWeapon: String,
        beliefSystem: String,
        description: String
    ) {
        viewModelScope.launch {
            val isEdit = id != null && id.isNotEmpty()
            if (!isEdit) {
                _uiState.update { it.copy(ethnographyAdd = EthnographyActionUIState.Loading) }
            } else {
                _uiState.update { it.copy(ethnographyChange = EthnographyActionUIState.Loading) }
            }

            val request = EthnographyRequest(
                tribeName = tribeName,
                region = region,
                language = language,
                traditionalHouse = traditionalHouse,
                traditionalWeapon = traditionalWeapon,
                beliefSystem = beliefSystem,
                description = description
            )

            try {
                val response = if (!isEdit) {
                    repository.postEthnography(authToken, request)
                } else {
                    repository.putEthnography(authToken, id!!, request)
                }

                if (response.status == "success") {
                    val successId = (response.data as? ResponseEthnographyAdd)?.ethnographyId ?: id
                    val successState = EthnographyActionUIState.Success(response.message, successId)
                    _uiState.update { state ->
                        if (!isEdit) state.copy(ethnographyAdd = successState)
                        else state.copy(ethnographyChange = successState)
                    }
                } else {
                    Log.e("EthnographyViewModel", "API Error Body: ${response.message}")
                    val errorState = EthnographyActionUIState.Error(response.message)
                    _uiState.update { state ->
                        if (!isEdit) state.copy(ethnographyAdd = errorState)
                        else state.copy(ethnographyChange = errorState)
                    }
                }
            } catch (e: Exception) {
                Log.e("EthnographyViewModel", "Upsert failed with exception", e)
                val errorState = EthnographyActionUIState.Error(e.message ?: "Unknown error")
                _uiState.update { state ->
                    if (!isEdit) state.copy(ethnographyAdd = errorState)
                    else state.copy(ethnographyChange = errorState)
                }
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

    fun getAllEthnographies(authToken: String, search: String? = null, region: String = "Semua") {
        currentSearch = search
        viewModelScope.launch {
            _uiState.update { it.copy(
                ethnographies = EthnographiesUIState.Loading,
                currentPage = 1,
                selectedRegion = region
            ) }
            
            // Note: Assuming API supports search. Adding filter logic locally if API doesn't support region param yet.
            // If repository/API supports pagination and region filtering, update repository call accordingly.
            val response = runCatching { repository.getEthnographies(authToken, search) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            var list = it.data?.ethnographies ?: emptyList()
                            if (region != "Semua") {
                                list = list.filter { item -> item.region.contains(region, ignoreCase = true) }
                            }
                            // Simulating first page
                            EthnographiesUIState.Success(list.take(10), isLastPage = list.size <= 10)
                        } else {
                            EthnographiesUIState.Error(it.message)
                        }
                    },
                    onFailure = { EthnographiesUIState.Error(it.message ?: "Unknown error") }
                ).let { state.copy(ethnographies = it) }
            }
        }
    }

    fun loadNextPage(authToken: String) {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || (currentState.ethnographies as? EthnographiesUIState.Success)?.isLastPage == true) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentState.currentPage + 1
            
            val response = runCatching { repository.getEthnographies(authToken, currentSearch) }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            var list = it.data?.ethnographies ?: emptyList()
                            if (state.selectedRegion != "Semua") {
                                list = list.filter { item -> item.region.contains(state.selectedRegion, ignoreCase = true) }
                            }
                            
                            val currentList = (state.ethnographies as? EthnographiesUIState.Success)?.data ?: emptyList()
                            val nextItems = list.drop(currentList.size).take(10)
                            val newList = currentList + nextItems
                            
                            state.copy(
                                ethnographies = EthnographiesUIState.Success(newList, isLastPage = newList.size >= list.size),
                                currentPage = nextPage,
                                isLoadingMore = false
                            )
                        } else {
                            state.copy(isLoadingMore = false)
                        }
                    },
                    onFailure = { 
                        state.copy(isLoadingMore = false)
                    }
                )
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
