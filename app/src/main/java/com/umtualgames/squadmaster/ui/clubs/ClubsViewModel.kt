package com.umtualgames.squadmaster.ui.clubs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.responses.item.Club
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClubsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<GetSquadListViewState>()
    val getViewState: LiveData<GetSquadListViewState> = viewState

    fun getSquadList() = viewModelScope.launch {
        viewState.postValue(GetSquadListViewState.LoadingState)
        repository.getSquadList().let {
            if (it.isSuccessful) viewState.postValue(GetSquadListViewState.SuccessState(it.body()!!.data, false)) else refreshTokenLogin(getRefreshToken())
        }
    }

    fun getSquadListByLeague(leagueID: Int, userID: Int, levelPass: Boolean = false) = viewModelScope.launch {
        viewState.postValue(GetSquadListViewState.LoadingState)
        repository.getSquadListByLeague(leagueID, userID).let {
            if (it.isSuccessful) viewState.postValue(GetSquadListViewState.SuccessState(it.body()!!.data, levelPass)) else refreshTokenLogin(getRefreshToken())
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(GetSquadListViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> {
                    viewState.postValue(GetSquadListViewState.RefreshState(it.body()!!.data.token))
                }
                else -> viewState.postValue(GetSquadListViewState.ErrorState(it.message()))
            }
        }
    }
}

sealed class GetSquadListViewState {
    object LoadingState : GetSquadListViewState()
    data class SuccessState(val response: List<Club>, val levelPass: Boolean) : GetSquadListViewState()
    data class ErrorState(val message: String) : GetSquadListViewState()
    data class WarningState(val message: String?) : GetSquadListViewState()
    data class RefreshState(val response: Token) : GetSquadListViewState()
}