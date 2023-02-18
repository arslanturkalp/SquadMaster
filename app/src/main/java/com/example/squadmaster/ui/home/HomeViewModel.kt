package com.example.squadmaster.ui.home

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.example.squadmaster.utils.applyThreads

class HomeViewModel: BaseViewModel() {
    
    private val viewState = MutableLiveData<HomeViewState>()
    val getViewState: LiveData<HomeViewState> = viewState

    fun getUserPoint(userId: Int) {
        compositeDisposable.addAll(
            remoteDataSource
                .getPoint(userId)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(HomeViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(HomeViewState.UserPointState(response))
                        }
                        Status.ERROR -> viewState.postValue(HomeViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class HomeViewState {
    object LoadingState : HomeViewState()
    data class ErrorState(val message: String) : HomeViewState()
    data class WarningState(val message: String?) : HomeViewState()
    data class RefreshState(val response: LoginResponse) : HomeViewState()
    data class UserPointState(val response: UserPointResponse) : HomeViewState()
}