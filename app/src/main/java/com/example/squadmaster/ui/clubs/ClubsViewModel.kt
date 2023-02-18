package com.example.squadmaster.ui.clubs

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.application.SessionManager
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.responses.item.Club
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.utils.applyThreads

class ClubsViewModel : BaseViewModel() {

    private val viewState = MutableLiveData<GetSquadListViewState>()
    val getViewState: LiveData<GetSquadListViewState> = viewState

    fun getSquadList() {
        compositeDisposable.addAll(
            remoteDataSource
                .getSquadList()
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadListViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadListViewState.SuccessState(response.data))
                        }
                        Status.ERROR -> {
                            refreshTokenLogin(SessionManager.getRefreshToken())
                        }
                    }
                }
        )
    }

    fun getSquadListByLeague(leagueID: Int) {
        compositeDisposable.addAll(
            remoteDataSource
                .getSquadListByLeague(leagueID)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadListViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadListViewState.SuccessState(response.data))
                        }
                        Status.ERROR -> {
                            refreshTokenLogin(SessionManager.getRefreshToken())
                        }
                    }
                }
        )
    }

    private fun refreshTokenLogin(refreshToken: String) {
        compositeDisposable.addAll(
            remoteDataSource
                .signInRefreshToken(refreshToken)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadListViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadListViewState.RefreshState(response))
                        }
                        Status.ERROR -> viewState.postValue(GetSquadListViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class GetSquadListViewState {
    object LoadingState : GetSquadListViewState()
    data class SuccessState(val response: List<Club>) : GetSquadListViewState()
    data class ErrorState(val message: String) : GetSquadListViewState()
    data class WarningState(val message: String?) : GetSquadListViewState()
    data class RefreshState(val response: LoginResponse) : GetSquadListViewState()
}