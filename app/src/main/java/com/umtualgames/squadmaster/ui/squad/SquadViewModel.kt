package com.umtualgames.squadmaster.ui.squad

import com.umtualgames.squadmaster.ui.base.BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.game.GameViewState
import com.umtualgames.squadmaster.utils.applyThreads

class SquadViewModel : BaseViewModel() {

    private val viewState = MutableLiveData<GetSquadViewState>()
    val getViewState: LiveData<GetSquadViewState> = viewState

    fun getSquad(squadName: String) {
        compositeDisposable.addAll(
            remoteDataSource
                .getSquad(squadName)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            when (response.statusCode) {
                                200 -> viewState.postValue(GetSquadViewState.SuccessState(response))
                                else -> viewState.postValue(GetSquadViewState.WarningState(response.message))
                            }
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
                        Status.LOADING -> viewState.postValue(GetSquadViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data
                            if (response?.data?.token != null) {
                                viewState.postValue(GetSquadViewState.RefreshState(response.data.token))
                            } else {
                                viewState.postValue(GetSquadViewState.ReturnSplashState)
                            }
                        }
                        Status.ERROR -> viewState.postValue(GetSquadViewState.ErrorState(it.message!!))
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
                        Status.LOADING -> viewState.postValue(GetSquadViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadViewState.UserPointState(response))
                        }
                        Status.ERROR -> refreshTokenLogin(getRefreshToken())
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
                        Status.LOADING -> viewState.postValue(GetSquadViewState.UserPointLoadingState)
                        Status.SUCCESS -> {
                            viewState.postValue(GetSquadViewState.UpdateState)
                        }
                        Status.ERROR -> refreshTokenLogin(getRefreshToken())
                    }
                }
        )
    }

    fun levelPass(levelPassRequest: LevelPassRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .levelPass(levelPassRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadViewState.UserPointLoadingState)
                        Status.SUCCESS -> {
                            viewState.postValue(GetSquadViewState.LevelPassState(it.data!!))
                        }
                        Status.ERROR -> refreshTokenLogin(getRefreshToken())
                    }
                }
        )
    }
}

sealed class GetSquadViewState {
    object LoadingState : GetSquadViewState()
    object UserPointLoadingState : GetSquadViewState()
    object UpdateState : GetSquadViewState()
    object ReturnSplashState : GetSquadViewState()
    data class LevelPassState(val response: LevelPassResponse) : GetSquadViewState()
    data class SuccessState(val response: GetFirstElevenBySquadResponse) : GetSquadViewState()
    data class ErrorState(val message: String) : GetSquadViewState()
    data class WarningState(val message: String?) : GetSquadViewState()
    data class RefreshState(val response: Token) : GetSquadViewState()
    data class UserPointState(val response: UserPointResponse) : GetSquadViewState()
}