package com.example.squadmaster.ui.register

import BaseViewModel
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.R
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.network.requests.RegisterRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.utils.applyThreads
import com.google.android.material.checkbox.MaterialCheckBox

class RegisterViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<RegisterViewState>()
    val getViewState: LiveData<RegisterViewState> = viewState

    fun register(name: String, surname: String, username: String, password: String, email: String) {
        registerValidation(name, username, password, email)
        when (getErrorList().isNotEmpty()){
            true -> viewState.postValue(RegisterViewState.ValidationState(getErrorList()))
            false -> requestRegister(RegisterRequest(name, surname, username, password, email))
        }
    }

    private fun requestRegister(registerRequest: RegisterRequest) {
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

    private fun registerValidation(name: String, username: String, password: String, email: String) {
        clearErrorList()

        if (TextUtils.isEmpty(name.trim())) {
            addError(R.string.name_empty_warning)
        }

        if (TextUtils.isEmpty(username.trim())) {
            addError(R.string.username_empty_warning)
        }

        if (TextUtils.isEmpty(password)) {
            addError(R.string.password_empty_warning)
        }

        if (TextUtils.isEmpty(email.trim())) {
            addError(R.string.email_empty_warning)
        }
    }
}

sealed class RegisterViewState {
    object LoadingState : RegisterViewState()
    data class SuccessState(val response: Boolean) : RegisterViewState()
    data class AdminState(val response: LoginResponse) : RegisterViewState()
    data class ErrorState(val message: String) : RegisterViewState()
    data class WarningState(val message: String?) : RegisterViewState()
    data class ValidationState(val validationErrorList: List<Int>) : RegisterViewState()

}