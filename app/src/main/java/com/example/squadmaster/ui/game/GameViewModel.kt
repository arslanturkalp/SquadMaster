package com.example.squadmaster.ui.game

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.application.SessionManager
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.UpdatePointRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import com.example.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.example.squadmaster.utils.applyThreads

class GameViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<GameViewState>()
    val getViewState: LiveData<GameViewState> = viewState

    fun getSquad() {
        compositeDisposable.addAll(
            remoteDataSource
                .getRandomSquad()
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GameViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            when (response.statusCode) {
                                200 -> viewState.postValue(GameViewState.SuccessState(response))
                                else -> viewState.postValue(GameViewState.WarningState(response.message))
                            }
                        }
                        Status.ERROR -> { refreshTokenLogin(SessionManager.getRefreshToken()) }
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
                        Status.LOADING -> viewState.postValue(GameViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GameViewState.RefreshState(response))
                        }
                        Status.ERROR -> viewState.postValue(GameViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .updatePoint(updatePointRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GameViewState.ScoreLoadingState)
                        Status.SUCCESS -> { viewState.postValue(GameViewState.UpdateState) }
                        Status.ERROR -> viewState.postValue(GameViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

}

sealed class GameViewState {
    object LoadingState : GameViewState()
    object ScoreLoadingState : GameViewState()
    object UpdateState : GameViewState()
    data class SuccessState(val response: GetFirstElevenBySquadResponse) : GameViewState()
    data class ErrorState(val message: String) : GameViewState()
    data class WarningState(val message: String?) : GameViewState()
    data class RefreshState(val response: LoginResponse) : GameViewState()
}