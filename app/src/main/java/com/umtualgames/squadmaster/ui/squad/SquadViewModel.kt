package com.umtualgames.squadmaster.ui.squad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SquadViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<GetSquadViewState>()
    val getViewState: LiveData<GetSquadViewState> = viewState

    fun getSquad(squadName: String) = viewModelScope.launch {
        viewState.postValue(GetSquadViewState.LoadingState)
        repository.getFirstElevenBySquadName(squadName).let {
            when {
                it.isSuccessful -> viewState.postValue(GetSquadViewState.SuccessState(it.body()!!))
                it.code() != 200 -> viewState.postValue(GetSquadViewState.WarningState(it.message()))
                else -> refreshTokenLogin(getRefreshToken())
            }
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(GetSquadViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> viewState.postValue(GetSquadViewState.RefreshState(it.body()!!.data.token))
                it.code() != 200 -> viewState.postValue(GetSquadViewState.ReturnSplashState)
                else -> viewState.postValue(GetSquadViewState.ErrorState(it.message()))
            }
        }
    }

    fun getUserPoint(userId: Int) = viewModelScope.launch {
        viewState.postValue(GetSquadViewState.LoadingState)
        repository.getUserPoint(userId).let {
            if (it.isSuccessful) viewState.postValue(GetSquadViewState.UserPointState(it.body()!!)) else refreshTokenLogin(getRefreshToken())
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        viewState.postValue(GetSquadViewState.UserPointLoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) viewState.postValue(GetSquadViewState.UpdateState) else refreshTokenLogin(getRefreshToken())
        }
    }

    fun levelPass(levelPassRequest: LevelPassRequest) = viewModelScope.launch {
        viewState.postValue(GetSquadViewState.UserPointLoadingState)
        repository.levelPass(levelPassRequest).let {
            if (it.isSuccessful) viewState.postValue(GetSquadViewState.LevelPassState(it.body()!!)) else refreshTokenLogin(getRefreshToken())
        }
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