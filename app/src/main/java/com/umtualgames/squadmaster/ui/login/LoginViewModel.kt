package com.umtualgames.squadmaster.ui.login

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: Repository): BaseViewModel() {
    
    private val viewState = MutableLiveData<LoginViewState>()
    val getViewState: LiveData<LoginViewState> = viewState

    fun login(username: String, password: String) {
        loginValidation(username, password)
        when (getErrorList().isNotEmpty()){
            true -> viewState.postValue(LoginViewState.ValidationState(getErrorList()))
            false -> requestLogin(LoginRequest(username, password))
        }
    }

    private fun requestLogin(loginRequest: LoginRequest) = viewModelScope.launch{
        viewState.postValue(LoginViewState.LoadingState)
        repository.login(loginRequest).let {
            when {
                it.isSuccessful -> viewState.postValue(LoginViewState.SuccessState(it.body()!!))
                it.code() != 200 -> viewState.postValue(LoginViewState.WarningState(it.message()))
                else -> viewState.postValue(LoginViewState.ErrorState(it.message()))
            }
        }
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