package com.umtualgames.squadmaster.ui.gameover

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.utils.applyThreads

class GameOverViewModel: BaseViewModel() {

    private val viewState = MutableLiveData<GameOverViewState>()
    val getViewState: LiveData<GameOverViewState> = viewState

    private fun refreshTokenLogin(refreshToken: String) {
        compositeDisposable.addAll(
            remoteDataSource
                .signInRefreshToken(refreshToken)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GameOverViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GameOverViewState.RefreshState(response.data.token))
                        }
                        Status.ERROR -> viewState.postValue(GameOverViewState.ErrorState(it.message!!))
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
                        Status.LOADING -> viewState.postValue(GameOverViewState.ScoreLoadingState)
                        Status.SUCCESS -> viewState.postValue(GameOverViewState.UpdateState(it.data!!))
                        Status.ERROR -> { refreshTokenLogin(SessionManager.getRefreshToken()) }
                    }
                }
        )
    }
}


sealed class GameOverViewState {
    object LoadingState : GameOverViewState()
    object ScoreLoadingState : GameOverViewState()
    data class UpdateState(val response: UserPointResponse) : GameOverViewState()
    data class SuccessState(val response: GetFirstElevenBySquadResponse) : GameOverViewState()
    data class ErrorState(val message: String) : GameOverViewState()
    data class WarningState(val message: String?) : GameOverViewState()
    data class RefreshState(val response: Token) : GameOverViewState()
}