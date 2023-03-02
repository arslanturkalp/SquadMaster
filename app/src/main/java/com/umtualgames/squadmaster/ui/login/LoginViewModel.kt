package com.umtualgames.squadmaster.ui.login

import BaseViewModel
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.utils.applyThreads

class LoginViewModel: BaseViewModel() {
    
    private val viewState = MutableLiveData<LoginViewState>()
    val getViewState: LiveData<LoginViewState> = viewState

    fun login(username: String, password: String) {
        loginValidation(username, password)
        when (getErrorList().isNotEmpty()){
            true -> viewState.postValue(LoginViewState.ValidationState(getErrorList()))
            false -> requestLogin(LoginRequest(username, password))
        }
    }

    private fun requestLogin(loginRequest: LoginRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .login(loginRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(LoginViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            when (response.statusCode) {
                                200 -> viewState.postValue(LoginViewState.SuccessState(response))
                                else -> viewState.postValue(LoginViewState.WarningState(response.message))
                            }

                        }
                        Status.ERROR -> viewState.postValue(LoginViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

    private fun loginValidation(username: String, password: String) {
        clearErrorList()

        if (TextUtils.isEmpty(username.trim())) {
            addError(R.string.username_empty_warning)
        }

        if (TextUtils.isEmpty(password)) {
            addError(R.string.password_empty_warning)
        }
    }
}

sealed class LoginViewState {
    object LoadingState : LoginViewState()
    data class SuccessState(val response: LoginResponse) : LoginViewState()
    data class ErrorState(val message: String) : LoginViewState()
    data class WarningState(val message: String?) : LoginViewState()
    data class ValidationState(val validationErrorList: List<Int>) : LoginViewState()
}