package com.example.squadmaster.ui.squad

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.application.SessionManager.getRefreshToken
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.requests.UpdatePointRequest
import com.example.squadmaster.network.responses.item.Token
import com.example.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.example.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.example.squadmaster.utils.applyThreads

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
                        Status.LOADING -> viewState.postValue(GetSquadViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadViewState.RefreshState(response))
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
                        Status.ERROR -> viewState.postValue(GetSquadViewState.ErrorState(it.message!!))
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
                        Status.SUCCESS -> { viewState.postValue(GetSquadViewState.UpdateState)}
                        Status.ERROR -> viewState.postValue(GetSquadViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class GetSquadViewState {
    object LoadingState : GetSquadViewState()
    object UserPointLoadingState : GetSquadViewState()
    object UpdateState : GetSquadViewState()
    data class SuccessState(val response: GetFirstElevenBySquadResponse) : GetSquadViewState()
    data class ErrorState(val message: String) : GetSquadViewState()
    data class WarningState(val message: String?) : GetSquadViewState()
    data class RefreshState(val response: Token) : GetSquadViewState()
    data class UserPointState(val response: UserPointResponse) : GetSquadViewState()
}