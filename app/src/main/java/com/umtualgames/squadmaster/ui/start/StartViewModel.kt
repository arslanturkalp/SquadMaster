package com.umtualgames.squadmaster.ui.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.requests.RegisterRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RegisterResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {
    private val viewState = MutableLiveData<StartViewState>()
    val getViewState: LiveData<StartViewState> = viewState

    fun signIn(loginRequest: LoginRequest) = viewModelScope.launch {
        viewState.postValue(StartViewState.LoadingState)
        repository.login(loginRequest).let {
            when {
                it.isSuccessful -> viewState.postValue(StartViewState.SuccessState(it.body()!!))
                it.code() != 200 -> viewState.postValue(StartViewState.WarningState(it.message()))
                else -> viewState.postValue(StartViewState.ErrorState(it.message()))
            }
        }
    }

    fun register(registerRequest: RegisterRequest) = viewModelScope.launch {
        viewState.postValue(StartViewState.LoadingState)
        repository.register(registerRequest).let {
            if (it.isSuccessful) viewState.postValue(StartViewState.RegisterState(it.body()!!)) else viewState.postValue(StartViewState.ErrorState(it.message()))
        }
    }

    fun loginAdmin(loginRequest: LoginRequest) = viewModelScope.launch {
        viewState.postValue(StartViewState.LoadingState)
        repository.login(loginRequest).let {
            if (it.isSuccessful) viewState.postValue(StartViewState.AdminState(it.body()!!)) else viewState.postValue(StartViewState.ErrorState(it.message()))
        }
    }
}

sealed class StartViewState {
    object LoadingState : StartViewState()
    data class SuccessState(val response: LoginResponse) : StartViewState()
    data class AdminState(val response: LoginResponse) : StartViewState()
    data class RegisterState(val response: RegisterResponse) : StartViewState()
    data class ErrorState(val message: String) : StartViewState()
    data class WarningState(val message: String?) : StartViewState()
}