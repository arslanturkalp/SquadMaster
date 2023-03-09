package com.umtualgames.squadmaster.ui.home

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.utils.applyThreads

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
                            when (response.statusCode) {
                                200 -> {
                                    viewState.postValue(HomeViewState.UserPointState(response))
                                    getLeagues()
                                }
                                else -> viewState.postValue(HomeViewState.WarningState(response.message))
                            }
                        }
                        Status.ERROR -> {
                            refreshTokenLogin(getRefreshToken())
                        }
                    }
                }
        )
    }

    private fun getLeagues() {
        compositeDisposable.addAll(
            remoteDataSource
                .getLeagues(SessionManager.getUserID())
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(HomeViewState.LeagueLoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(HomeViewState.LeagueSuccessState(response.data))
                        }
                        Status.ERROR -> { viewState.postValue(HomeViewState.ErrorState(it.message!!)) }
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
                        Status.LOADING -> viewState.postValue(HomeViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(HomeViewState.RefreshState(response.data.token))
                        }
                        Status.ERROR -> viewState.postValue(HomeViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class HomeViewState {
    object LoadingState : HomeViewState()
    object LeagueLoadingState : HomeViewState()
    data class ErrorState(val message: String) : HomeViewState()
    data class WarningState(val message: String?) : HomeViewState()
    data class UserPointState(val response: UserPointResponse) : HomeViewState()
    data class LeagueSuccessState(val response: List<League>) : HomeViewState()
    data class RefreshState(val response: Token) : HomeViewState()
}