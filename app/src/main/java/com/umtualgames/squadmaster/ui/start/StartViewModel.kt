package com.umtualgames.squadmaster.ui.start

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.utils.applyThreads

class StartViewModel: BaseViewModel() {
    private val viewState = MutableLiveData<StartViewState>()
    val getViewState: LiveData<StartViewState> = viewState

    fun signIn(loginRequest: LoginRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .login(loginRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(StartViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            when (response.statusCode) {
                                200 -> viewState.postValue(StartViewState.SuccessState(response))
                                else -> viewState.postValue(StartViewState.WarningState(it.message!!))
                            }

                        }
                        Status.ERROR -> viewState.postValue(StartViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class StartViewState {
    object LoadingState : StartViewState()
    data class SuccessState(val response: LoginResponse) : StartViewState()
    data class ErrorState(val message: String) : StartViewState()
    data class WarningState(val message: String?) : StartViewState()
}