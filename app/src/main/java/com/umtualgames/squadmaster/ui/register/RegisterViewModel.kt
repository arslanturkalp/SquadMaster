package com.umtualgames.squadmaster.ui.register

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.R
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RegisterResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<RegisterViewState>()
    val getViewState: LiveData<RegisterViewState> = viewState

    private var containsLower: Boolean = false
    private var containsUpper: Boolean = false
    private var containsDigit: Boolean = false

    fun register(name: String, surname: String, username: String, password: String, email: String) {
        registerValidation(name, username, password, email)
        when (getErrorList().isNotEmpty()) {
            true -> viewState.postValue(RegisterViewState.ValidationState(getErrorList()))
            false -> requestRegister(com.umtualgames.squadmaster.domain.entities.requests.RegisterRequest(name, surname, username, password, email))
        }
    }

    private fun requestRegister(registerRequest: com.umtualgames.squadmaster.domain.entities.requests.RegisterRequest) = viewModelScope.launch {
        viewState.postValue(RegisterViewState.LoadingState)
        repository.register(registerRequest).let {
            if (it.isSuccessful) {
                viewState.postValue(RegisterViewState.SuccessState(it.body()!!))
            } else {
                viewState.postValue(RegisterViewState.ErrorState(it.message()))
            }
        }
    }

    fun loginAdmin(loginRequest: com.umtualgames.squadmaster.domain.entities.requests.LoginRequest) = viewModelScope.launch {
        repository.login(loginRequest).let {
            if (it.isSuccessful) {
                viewState.postValue(RegisterViewState.AdminState(it.body()!!))
            } else {
                viewState.postValue(RegisterViewState.ErrorState(it.message()))
            }
        }
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

        if (!TextUtils.isEmpty(password)) {
            password.forEach {
                val char = it
                if (char.isLowerCase()) containsLower = true
                if (char.isUpperCase()) containsUpper = true
                if (char.isDigit()) containsDigit = true
            }

            if (!containsLower || !containsUpper || !containsDigit || password.length < 8) {
                addError(R.string.password_validation)
            }
        }
    }
}

sealed class RegisterViewState {
    object LoadingState : RegisterViewState()
    data class SuccessState(val response: RegisterResponse) : RegisterViewState()
    data class AdminState(val response: LoginResponse) : RegisterViewState()
    data class ErrorState(val message: String) : RegisterViewState()
    data class WarningState(val message: String?) : RegisterViewState()
    data class ValidationState(val validationErrorList: List<Int>) : RegisterViewState()

}