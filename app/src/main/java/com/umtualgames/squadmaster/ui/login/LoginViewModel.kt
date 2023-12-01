package com.umtualgames.squadmaster.ui.login

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.usecases.LoginUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase) : BaseViewModel() {

    private val viewState = MutableLiveData<LoginViewState>()
    val getViewState: LiveData<LoginViewState> = viewState

    private val _loginFlow: MutableStateFlow<Result<LoginResponse>> = MutableStateFlow(Result.Loading())
    val loginFlow: StateFlow<Result<LoginResponse>> = _loginFlow

    fun login(username: String, password: String) {
        loginValidation(username, password)
        when (getErrorList().isNotEmpty()) {
            true -> viewState.postValue(LoginViewState.ValidationState(getErrorList()))
            false -> requestLogin(LoginRequest(username, password))
        }
    }

    private fun requestLogin(loginRequest: LoginRequest) = viewModelScope.launch {
        loginUseCase(loginRequest).collect {
            when (it) {
                is Result.Error -> _loginFlow.emit(it)
                is Result.Loading -> _loginFlow.emit(it)
                is Result.Success -> _loginFlow.emit(it)
                is Result.Auth -> _loginFlow.emit(it)
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
    data class ValidationState(val validationErrorList: List<Int>) : LoginViewState()
}