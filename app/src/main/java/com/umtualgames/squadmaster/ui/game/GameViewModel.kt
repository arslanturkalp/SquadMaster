package com.umtualgames.squadmaster.ui.game

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.utils.applyThreads

class GameViewModel : BaseViewModel() {

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
                        Status.ERROR -> { refreshTokenLogin(getRefreshToken()) }
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
                            viewState.postValue(GameViewState.RefreshState(response.data.token))
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
                        Status.SUCCESS -> viewState.postValue(GameViewState.UpdateState)
                        Status.ERROR -> { refreshTokenLogin(getRefreshToken()) }
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
    data class RefreshState(val response: Token) : GameViewState()
}