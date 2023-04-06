package com.umtualgames.squadmaster.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<GameViewState>()
    val getViewState: LiveData<GameViewState> = viewState

    fun getSquad() = viewModelScope.launch {
        viewState.postValue(GameViewState.LoadingState)
        repository.getFirstElevenByRandomSquad().let {
            when {
                it.isSuccessful -> viewState.postValue(GameViewState.SuccessState(it.body()!!))
                it.code() != 200 -> viewState.postValue(GameViewState.WarningState(it.message()))
                else -> refreshTokenLogin(getRefreshToken())
            }
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(GameViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> viewState.postValue(GameViewState.RefreshState(it.body()!!.data.token))
                it.code() != 200 -> viewState.postValue(GameViewState.ReturnSplashState)
                else -> viewState.postValue(GameViewState.ErrorState(it.message()))
            }
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch{
        viewState.postValue(GameViewState.ScoreLoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) viewState.postValue(GameViewState.UpdateState(it.body()!!)) else refreshTokenLogin(getRefreshToken())
        }
    }
}

sealed class GameViewState {
    object LoadingState : GameViewState()
    object ScoreLoadingState : GameViewState()
    object ReturnSplashState : GameViewState()
    data class UpdateState(val response: UserPointResponse) : GameViewState()
    data class SuccessState(val response: GetFirstElevenBySquadResponse) : GameViewState()
    data class ErrorState(val message: String) : GameViewState()
    data class WarningState(val message: String?) : GameViewState()
    data class RefreshState(val response: Token) : GameViewState()
}