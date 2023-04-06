package com.umtualgames.squadmaster.ui.gameover

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
class GameOverViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<GameOverViewState>()
    val getViewState: LiveData<GameOverViewState> = viewState

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(GameOverViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            if (it.isSuccessful) viewState.postValue(GameOverViewState.RefreshState(it.body()!!.data.token)) else viewState.postValue(GameOverViewState.ErrorState(it.message()))
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        viewState.postValue(GameOverViewState.LoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) viewState.postValue(GameOverViewState.UpdateState(it.body()!!)) else refreshTokenLogin(getRefreshToken())
        }
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