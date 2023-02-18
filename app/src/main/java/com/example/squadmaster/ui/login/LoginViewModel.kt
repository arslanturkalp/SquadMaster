package com.example.squadmaster.ui.login

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.utils.applyThreads

class LoginViewModel: BaseViewModel() {
    
    private val viewState = MutableLiveData<LoginViewState>()
    val getViewState: LiveData<LoginViewState> = viewState

    fun signIn(loginRequest: LoginRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .login(loginRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(LoginViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(LoginViewState.SuccessState(response))
                        }
                        Status.ERROR -> viewState.postValue(LoginViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class LoginViewState {
    object LoadingState : LoginViewState()
    data class SuccessState(val response: LoginResponse) : LoginViewState()
    data class ErrorState(val message: String) : LoginViewState()
    data class WarningState(val message: String?) : LoginViewState()
}