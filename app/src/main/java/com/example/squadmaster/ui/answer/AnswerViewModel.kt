package com.example.squadmaster.ui.answer

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.example.squadmaster.utils.applyThreads

class AnswerViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<AnswerViewState>()
    val getViewState: LiveData<AnswerViewState> = viewState

    private fun refreshTokenLogin(refreshToken: String) {
        compositeDisposable.addAll(
            remoteDataSource
                .signInRefreshToken(refreshToken)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(AnswerViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(AnswerViewState.RefreshState(response))
                        }
                        Status.ERROR -> viewState.postValue(AnswerViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class AnswerViewState {
    object LoadingState : AnswerViewState()
    data class ErrorState(val message: String) : AnswerViewState()
    data class WarningState(val message: String?) : AnswerViewState()
    data class RefreshState(val response: LoginResponse) : AnswerViewState()
    data class UserPointState(val response: UserPointResponse) : AnswerViewState()
}