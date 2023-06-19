package com.umtualgames.squadmaster.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager.getRefreshToken
import com.umtualgames.squadmaster.application.SessionManager.getUserID
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<HomeViewState>()
    val getViewState: LiveData<HomeViewState> = viewState

    fun getUserPoint(userId: Int) = viewModelScope.launch {
        viewState.postValue(HomeViewState.LoadingState)
        repository.getUserPoint(userId).let {
            when {
                it.isSuccessful -> {
                    getLeagues()
                    getRoomCount()
                    viewState.postValue(HomeViewState.UserPointState(it.body()!!))
                }

                it.code() != 200 -> viewState.postValue(HomeViewState.WarningState(it.message()))
                else -> refreshTokenLogin(getRefreshToken())
            }
        }
    }

    private fun getLeagues() = viewModelScope.launch {
        viewState.postValue(HomeViewState.LeagueLoadingState)
        repository.getLeagues(getUserID()).let {
            if (it.isSuccessful) {
                viewState.postValue(HomeViewState.LeagueSuccessState(it.body()!!.data))
            } else {
                viewState.postValue(HomeViewState.ErrorState(it.message()))
            }
        }
    }

    private fun getRoomCount() = viewModelScope.launch {
        viewState.postValue(HomeViewState.LoadingState)
        repository.getRooms().let {
            when (it.body()?.statusCode) {
                200 -> viewState.postValue(HomeViewState.RoomCountState(it.body()?.data?.availableCount!!))
                300 -> viewState.postValue(HomeViewState.RoomCountState(0))
            }
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(HomeViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> {
                    viewState.postValue(HomeViewState.RefreshState(it.body()!!.data.token))
                }

                it.code() != 200 -> viewState.postValue(HomeViewState.ReturnSplashState)
                else -> viewState.postValue(HomeViewState.ErrorState(it.message()))
            }
        }
    }
}

sealed class HomeViewState {
    object LoadingState : HomeViewState()
    object LeagueLoadingState : HomeViewState()
    object ReturnSplashState : HomeViewState()
    data class ErrorState(val message: String) : HomeViewState()
    data class WarningState(val message: String?) : HomeViewState()
    data class UserPointState(val response: UserPointResponse) : HomeViewState()
    data class LeagueSuccessState(val response: List<League>) : HomeViewState()
    data class RoomCountState(val response: Int) : HomeViewState()
    data class RefreshState(val response: Token) : HomeViewState()
}