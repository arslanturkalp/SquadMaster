package com.umtualgames.squadmaster.ui.clubs

import com.umtualgames.squadmaster.ui.base.BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.data.enums.Status
import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.utils.applyThreads

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
                            viewState.postValue(GetSquadListViewState.SuccessState(response.data, false))
                        }
                        Status.ERROR -> {
                            refreshTokenLogin(getRefreshToken())
                        }
                    }
                }
        )
    }

    fun getSquadListByLeague(leagueID: Int, userID: Int, levelPass: Boolean = false) {
        compositeDisposable.addAll(
            remoteDataSource
                .getSquadListByLeague(leagueID, userID)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(GetSquadListViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadListViewState.SuccessState(response.data, levelPass))
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
                        Status.LOADING -> viewState.postValue(GetSquadListViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(GetSquadListViewState.RefreshState(response.data.token))
                        }
                        Status.ERROR -> viewState.postValue(GetSquadListViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}

sealed class GetSquadListViewState {
    object LoadingState : GetSquadListViewState()
    data class SuccessState(val response: List<Club>, val levelPass: Boolean) : GetSquadListViewState()
    data class ErrorState(val message: String) : GetSquadListViewState()
    data class WarningState(val message: String?) : GetSquadListViewState()
    data class RefreshState(val response: Token) : GetSquadListViewState()
}