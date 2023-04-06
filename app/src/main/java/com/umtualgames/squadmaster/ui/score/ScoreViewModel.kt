package com.umtualgames.squadmaster.ui.score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<ScoreViewState>()
    val getViewState: LiveData<ScoreViewState> = viewState

    private fun getRankList() = viewModelScope.launch {
        viewState.postValue(ScoreViewState.LoadingState)
        repository.getRankList().let {
            if (it.isSuccessful) viewState.postValue(ScoreViewState.SuccessState(it.body()!!)) else viewState.postValue(ScoreViewState.ErrorState(it.message()))
        }
    }

    fun getUserPoint(userId: Int) = viewModelScope.launch {
        viewState.postValue(ScoreViewState.LoadingState)
        repository.getUserPoint(userId).let {
            if (it.isSuccessful) {
                getRankList()
                viewState.postValue(ScoreViewState.UserPointState(it.body()!!))
            } else {
                refreshTokenLogin(getRefreshToken())
            }
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(ScoreViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> {
                    viewState.postValue(ScoreViewState.RefreshState(it.body()!!.data.token))
                }
                else -> viewState.postValue(ScoreViewState.ErrorState(it.message()))
            }
        }
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