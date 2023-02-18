package com.example.squadmaster.ui.register

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.network.requests.RegisterRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.utils.applyThreads

class RegisterViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<RegisterViewState>()
    val getViewState: LiveData<RegisterViewState> = viewState

    fun register(registerRequest: RegisterRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .register(registerRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(RegisterViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(RegisterViewState.SuccessState(response))
                        }
                        Status.ERROR -> viewState.postValue(RegisterViewState.ErrorState(it.message!!))
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
                        Status.LOADING -> viewState.postValue(RegisterViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(RegisterViewState.AdminState(response))
                        }
                        Status.ERROR -> viewState.postValue(RegisterViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}


sealed class RegisterViewState {
    object LoadingState : RegisterViewState()
    data class SuccessState(val response: Boolean) : RegisterViewState()
    data class AdminState(val response: LoginResponse) : RegisterViewState()
    data class ErrorState(val message: String) : RegisterViewState()
    data class WarningState(val message: String?) : RegisterViewState()
}