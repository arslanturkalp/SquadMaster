package com.example.squadmaster.ui.start

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.example.squadmaster.ui.squad.GetSquadViewState
import com.example.squadmaster.utils.applyThreads

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
                            viewState.postValue(StartViewState.SuccessState(response))
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