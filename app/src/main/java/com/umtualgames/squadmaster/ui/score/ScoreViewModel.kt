package com.umtualgames.squadmaster.ui.score

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.home.HomeViewState
import com.umtualgames.squadmaster.utils.applyThreads

class ScoreViewModel : BaseViewModel() {

    private val viewState = MutableLiveData<ScoreViewState>()
    val getViewState: LiveData<ScoreViewState> = viewState

    private fun getRankList() {
        compositeDisposable.addAll(
            remoteDataSource
                .getRankList()
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(ScoreViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(ScoreViewState.SuccessState(response))
                        }
                        Status.ERROR -> viewState.postValue(ScoreViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

    fun getUserPoint(userId: Int) {
        compositeDisposable.addAll(
            remoteDataSource
                .getPoint(userId)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(ScoreViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            getRankList()
                            viewState.postValue(ScoreViewState.UserPointState(response))
                        }
                        Status.ERROR -> {
                            refreshTokenLogin(getRefreshToken())
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
                        Status.LOADING -> viewState.postValue(ScoreViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(ScoreViewState.RefreshState(response))
                        }
                        Status.ERROR -> viewState.postValue(ScoreViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}


sealed class ScoreViewState {
    object LoadingState : ScoreViewState()
    data class SuccessState(val response: GetRankListResponse) : ScoreViewState()
    data class ErrorState(val message: String) : ScoreViewState()
    data class WarningState(val message: String?) : ScoreViewState()
    data class UserPointState(val response: UserPointResponse) : ScoreViewState()
    data class RefreshState(val response: Token) : ScoreViewState()
}