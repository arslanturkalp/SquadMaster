package com.umtualgames.squadmaster.ui.splash

import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.entities.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.domain.usecases.LoginUseCase
import com.umtualgames.squadmaster.domain.usecases.ProjectSettingUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val projectSettingUseCase: ProjectSettingUseCase,
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {

    private val _projectSettingFlow: MutableStateFlow<Result<ProjectSettingsResponse>> = MutableStateFlow(Result.Loading())
    val projectSettingFlow: StateFlow<Result<ProjectSettingsResponse>> = _projectSettingFlow

    private val _loginFlow: MutableStateFlow<Result<LoginResponse>> = MutableStateFlow(Result.Loading())
    val loginFlow: StateFlow<Result<LoginResponse>> = _loginFlow

    fun getProjectSettings() = viewModelScope.launch {
        projectSettingUseCase().collect {
            when (it) {
                is Result.Error -> _projectSettingFlow.emit(it)
                is Result.Loading -> _projectSettingFlow.emit(it)
                is Result.Success -> _projectSettingFlow.emit(it)
                is Result.Auth -> _projectSettingFlow.emit(it)
            }
        }
    }

    fun loginAdmin(loginRequest: LoginRequest) = viewModelScope.launch {
        loginUseCase(loginRequest).collect {
            when (it) {
                is Result.Error -> _loginFlow.emit(it)
                is Result.Loading -> _loginFlow.emit(it)
                is Result.Success -> _loginFlow.emit(it)
                is Result.Auth -> _loginFlow.emit(it)
            }
        }
    }
}