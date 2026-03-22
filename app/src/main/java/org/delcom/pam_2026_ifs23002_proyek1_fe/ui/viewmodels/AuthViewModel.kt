package org.delcom.pam_2026_ifs23002_proyek1_fe.ui.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.service.IEthnographyRepository
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.RequestAuthLogin
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.RequestAuthLogout
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.RequestAuthRefreshToken
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.RequestAuthRegister
import org.delcom.pam_2026_ifs23002_proyek1_fe.network.ethnography.data.ResponseAuthLogin
import org.delcom.pam_2026_ifs23002_proyek1_fe.prefs.AuthTokenPref
import javax.inject.Inject

sealed interface AuthUIState {
    data class Success(val data: ResponseAuthLogin) : AuthUIState
    data class Error(val message: String) : AuthUIState
    object Loading : AuthUIState
    object Idle : AuthUIState
}

sealed interface AuthActionUIState {
    data class Success(val message: String) : AuthActionUIState
    data class Error(val message: String) : AuthActionUIState
    object Loading : AuthActionUIState
    object Idle : AuthActionUIState
}

sealed interface AuthLogoutUIState {
    data class Success(val message: String) : AuthLogoutUIState
    data class Error(val message: String) : AuthLogoutUIState
    object Loading : AuthLogoutUIState
    object Idle : AuthLogoutUIState
}

data class UIStateAuth(
    val auth: AuthUIState = AuthUIState.Idle,
    var authRegister: AuthActionUIState = AuthActionUIState.Idle,
    var authLogout: AuthLogoutUIState = AuthLogoutUIState.Idle,
    var authRefreshToken: AuthActionUIState = AuthActionUIState.Idle,
)

@HiltViewModel
@Keep
class AuthViewModel @Inject constructor(
    private val repository: IEthnographyRepository,
    private val authTokenPref: AuthTokenPref
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateAuth())
    val uiState = _uiState.asStateFlow()

    fun register(name: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authRegister = AuthActionUIState.Loading) }
            val response = runCatching {
                repository.postRegister(RequestAuthRegister(name = name, username = username, password = password))
            }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") AuthActionUIState.Success(it.data!!.userId)
                        else AuthActionUIState.Error(it.message)
                    },
                    onFailure = { 
                        Log.e("AuthViewModel", "Register failed", it)
                        AuthActionUIState.Error(it.message ?: "Unknown error") 
                    }
                ).let { state.copy(authRegister = it) }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }
            val response = runCatching {
                repository.postLogin(RequestAuthLogin(username = username, password = password))
            }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success" && it.data != null) {
                            viewModelScope.launch {
                                authTokenPref.saveAuthToken(it.data.authToken)
                                authTokenPref.saveRefreshToken(it.data.refreshToken)
                            }
                            AuthUIState.Success(it.data)
                        } else AuthUIState.Error(it.message)
                    },
                    onFailure = { 
                        Log.e("AuthViewModel", "Login failed", it)
                        AuthUIState.Error(it.message ?: "Unknown error") 
                    }
                ).let { state.copy(auth = it) }
            }
        }
    }

    fun logout(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authLogout = AuthLogoutUIState.Loading) }
            authTokenPref.clearTokens()
            val response = runCatching {
                repository.postLogout(RequestAuthLogout(authToken = authToken))
            }
            _uiState.update { state ->
                response.fold(
                    onSuccess = { AuthLogoutUIState.Success(it.message) },
                    onFailure = { 
                        Log.e("AuthViewModel", "Logout failed", it)
                        AuthLogoutUIState.Error(it.message ?: "Unknown error") 
                    }
                ).let { state.copy(authLogout = it, auth = AuthUIState.Idle) }
            }
        }
    }

    fun refreshToken(authToken: String, refreshToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading, authRefreshToken = AuthActionUIState.Loading) }
            val response = runCatching {
                repository.postRefreshToken(RequestAuthRefreshToken(authToken = authToken, refreshToken = refreshToken))
            }
            _uiState.update { state ->
                response.fold(
                    onSuccess = {
                        if (it.status == "success") {
                            viewModelScope.launch {
                                authTokenPref.saveAuthToken(it.data!!.authToken)
                                authTokenPref.saveRefreshToken(it.data.refreshToken)
                            }
                            state.copy(auth = AuthUIState.Success(it.data!!), authRefreshToken = AuthActionUIState.Success(it.message))
                        } else state.copy(auth = AuthUIState.Error(it.message), authRefreshToken = AuthActionUIState.Error(it.message))
                    },
                    onFailure = {
                        Log.e("AuthViewModel", "Refresh token failed", it)
                        state.copy(auth = AuthUIState.Error(it.message ?: "Unknown error"), authRefreshToken = AuthActionUIState.Error(it.message ?: "Unknown error"))
                    }
                )
            }
        }
    }

    fun loadTokenFromPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }
            val authToken = authTokenPref.authToken.first()
            val refreshToken = authTokenPref.refreshToken.first()

            _uiState.update { state ->
                if (authToken.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
                    state.copy(auth = AuthUIState.Error("Token tidak tersedia"))
                } else {
                    state.copy(auth = AuthUIState.Success(ResponseAuthLogin(authToken = authToken, refreshToken = refreshToken)))
                }
            }
        }
    }
}
