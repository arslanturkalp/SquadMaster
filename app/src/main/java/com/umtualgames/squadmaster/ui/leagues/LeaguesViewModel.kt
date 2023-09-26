package com.umtualgames.squadmaster.ui.leagues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaguesViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<LeaguesViewState>()
    val getViewState: LiveData<LeaguesViewState> = viewState

    fun getLeagues(userID: Int) = viewModelScope.launch {
        viewState.postValue(LeaguesViewState.LoadingState)
        repository.getLeagues(userID).let {
            if (it.isSuccessful) {
                viewState.postValue(LeaguesViewState.SuccessState(it.body()?.data.orEmpty()))
            } else {
                viewState.postValue(LeaguesViewState.ErrorState(it.message()))
            }
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        viewState.postValue(LeaguesViewState.UserPointLoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) {
                viewState.postValue(LeaguesViewState.UpdateState)
            } else {
                viewState.postValue(LeaguesViewState.ErrorState(it.message()))
            }
        }
    }

    fun getUserPoint(userId: Int) = viewModelScope.launch {
        viewState.postValue(LeaguesViewState.LoadingState)
        repository.getUserPoint(userId).let {
            when {
                it.isSuccessful -> {
                    getLeagues(userId)
                    viewState.postValue(LeaguesViewState.UserPointState(it.body()!!))
                }
                it.code() == 401 -> refreshTokenLogin(getRefreshToken())
                else -> {
                    viewState.postValue(LeaguesViewState.WarningState(it.message()))
                }
            }
        }
    }


    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(LeaguesViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> {
                    viewState.postValue(LeaguesViewState.RefreshTokenState(it.body()!!.data.token))
                }
                it.code() != 200 -> viewState.postValue(LeaguesViewState.ReturnSplashState)
                else -> viewState.postValue(LeaguesViewState.ErrorState(it.message()))
            }
        }
    }

}


sealed class LeaguesViewState {
    object LoadingState : LeaguesViewState()
    object UserPointLoadingState : LeaguesViewState()
    object ReturnSplashState : LeaguesViewState()
    object UpdateState : LeaguesViewState()
    data class SuccessState(val response: List<League>) : LeaguesViewState()
    data class ErrorState(val message: String) : LeaguesViewState()
    data class WarningState(val message: String?) : LeaguesViewState()
    data class UserPointState(val response: UserPointResponse) : LeaguesViewState()
    data class RefreshState(val response: LoginResponse) : LeaguesViewState()
    data class RefreshTokenState(val response: Token) : LeaguesViewState()
}