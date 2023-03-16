package com.umtualgames.squadmaster.ui.splash

import com.umtualgames.squadmaster.ui.base.BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.network.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.utils.applyThreads

class SplashViewModel : BaseViewModel() {

    private val viewState = MutableLiveData<SplashViewState>()
    val getViewState: LiveData<SplashViewState> = viewState

    fun getProjectSettings() {
        compositeDisposable.addAll(
            remoteDataSource
                .getProjectSettings()
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(SplashViewState.LoadingState)
                        Status.SUCCESS -> viewState.postValue(SplashViewState.SuccessState(it.data!!))
                        Status.ERROR -> viewState.postValue(SplashViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

    fun loginAdmin(loginRequest: LoginRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .login(loginRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> {}
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(SplashViewState.AdminState(response))
                        }
                        Status.ERROR -> viewState.postValue(SplashViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

}

sealed class SplashViewState {
    object LoadingState : SplashViewState()
    data class SuccessState(val response: ProjectSettingsResponse) : SplashViewState()
    data class AdminState(val response: LoginResponse) : SplashViewState()
    data class ErrorState(val message: String) : SplashViewState()
}