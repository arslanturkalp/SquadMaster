package com.umtualgames.squadmaster.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.network.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: Repository): BaseViewModel() {

    private val viewState = MutableLiveData<SplashViewState>()
    val getViewState: LiveData<SplashViewState> = viewState

    fun getProjectSettings() = viewModelScope.launch {
        repository.getProjectSettings().let {
            if (it.isSuccessful) viewState.postValue(SplashViewState.SuccessState(it.body()!!)) else viewState.postValue(SplashViewState.ErrorState(it.message()))
        }
    }

    fun loginAdmin(loginRequest: LoginRequest) = viewModelScope.launch {
        repository.login(loginRequest).let {
            if (it.isSuccessful) viewState.postValue(SplashViewState.AdminState(it.body()!!)) else viewState.postValue(SplashViewState.ErrorState(it.message()))
        }
    }
}

sealed class SplashViewState {
    object LoadingState : SplashViewState()
    data class SuccessState(val response: ProjectSettingsResponse) : SplashViewState()
    data class AdminState(val response: LoginResponse) : SplashViewState()
    data class ErrorState(val message: String) : SplashViewState()
}