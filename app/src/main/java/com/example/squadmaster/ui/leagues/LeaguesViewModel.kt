package com.example.squadmaster.ui.leagues

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.application.SessionManager.getUserID
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.responses.item.League
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.utils.applyThreads

class LeaguesViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<LeaguesViewState>()
    val getViewState: LiveData<LeaguesViewState> = viewState

    fun getLeagues() {
        compositeDisposable.addAll(
            remoteDataSource
                .getLeagues(getUserID())
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(LeaguesViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(LeaguesViewState.SuccessState(response.data))
                        }
                        Status.ERROR -> { viewState.postValue(LeaguesViewState.ErrorState(it.message!!)) }
                    }
                }
        )
    }
}


sealed class LeaguesViewState {
    object LoadingState : LeaguesViewState()
    data class SuccessState(val response: List<League>) : LeaguesViewState()
    data class ErrorState(val message: String) : LeaguesViewState()
    data class WarningState(val message: String?) : LeaguesViewState()
    data class RefreshState(val response: LoginResponse) : LeaguesViewState()
}