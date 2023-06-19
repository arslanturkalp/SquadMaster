package com.umtualgames.squadmaster.ui.online

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.network.responses.roomresponses.CreateRoomResponse
import com.umtualgames.squadmaster.network.responses.roomresponses.GetRoomsResponse
import com.umtualgames.squadmaster.network.responses.roomresponses.JoinRoomResponse
import com.umtualgames.squadmaster.network.responses.roomresponses.LeaveRoomResponse
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<OnlineViewState>()
    val getViewState: LiveData<OnlineViewState> = viewState

    fun getAvailableRoomCount() = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.getRooms().let {
            when (it.body()?.statusCode) {
                200 -> viewState.postValue(OnlineViewState.RoomCountState(it.body()?.data?.availableCount!!))
                else -> viewState.postValue(OnlineViewState.RoomCountState(0))
            }
        }
    }

    fun createRoom(roomName: String) = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.createRoom(roomName).let {
            when {
                it.isSuccessful -> viewState.postValue(OnlineViewState.CreateRoomState(it.body()!!))
            }
        }
    }

    fun leaveRoom(roomName: String) = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.leaveRoom(roomName).let {
            when {
                it.isSuccessful -> viewState.postValue(OnlineViewState.LeaveRoomState(it.body()!!))
            }
        }
    }

    fun joinRoom() = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.joinRoom().let {
            when {
                it.isSuccessful -> viewState.postValue(OnlineViewState.JoinRoomState(it.body()!!))
            }
        }
    }

    fun getRooms() = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.getRooms().let {
            when {
                it.isSuccessful -> viewState.postValue(OnlineViewState.RoomsState(it.body()!!))
            }
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        viewState.postValue(OnlineViewState.UserPointLoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) viewState.postValue(OnlineViewState.UpdateState) else refreshTokenLogin(SessionManager.getRefreshToken())
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(OnlineViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> viewState.postValue(OnlineViewState.RefreshState(it.body()!!.data.token))
                it.code() != 200 -> viewState.postValue(OnlineViewState.ReturnSplashState)
                else -> viewState.postValue(OnlineViewState.ErrorState(it.message()))
            }
        }
    }
}


sealed class OnlineViewState {
    object LoadingState : OnlineViewState()
    object UserPointLoadingState : OnlineViewState()
    object ReturnSplashState : OnlineViewState()
    object UpdateState : OnlineViewState()
    data class CreateRoomState(val response: CreateRoomResponse) : OnlineViewState()
    data class LeaveRoomState(val response: LeaveRoomResponse) : OnlineViewState()
    data class JoinRoomState(val response: JoinRoomResponse) : OnlineViewState()
    data class RoomsState(val response: GetRoomsResponse) : OnlineViewState()
    data class RoomCountState(val response: Int) : OnlineViewState()
    data class RefreshState(val response: Token) : OnlineViewState()
    data class ErrorState(val message: String) : OnlineViewState()
    data class WarningState(val message: String?) : OnlineViewState()
}