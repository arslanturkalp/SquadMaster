package com.umtualgames.squadmaster.ui.game

import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.usecases.GetFirstElevenRandomUseCase
import com.umtualgames.squadmaster.domain.usecases.RefreshTokenUseCase
import com.umtualgames.squadmaster.domain.usecases.UpdatePointUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val getSquadRandomUseCase: GetFirstElevenRandomUseCase,
    private val updatePointUseCase: UpdatePointUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
) : BaseViewModel() {

    private val _squadFlow: MutableStateFlow<Result<GetFirstElevenBySquadResponse>> = MutableStateFlow(Result.Loading())
    val squadFlow: StateFlow<Result<GetFirstElevenBySquadResponse>> = _squadFlow

    private val _updatePointFlow: MutableStateFlow<Result<UserPointResponse>> = MutableStateFlow(Result.Loading())
    val updatePointFlow: StateFlow<Result<UserPointResponse>> = _updatePointFlow

    private val _refreshTokenFlow: MutableStateFlow<Result<RefreshTokenResponse>> = MutableStateFlow(Result.Loading())
    val refreshTokenFlow: StateFlow<Result<RefreshTokenResponse>> = _refreshTokenFlow

    fun getSquad() = viewModelScope.launch {
        getSquadRandomUseCase().collect {
            when (it) {
                is Result.Error -> _squadFlow.emit(it)
                is Result.Loading -> _squadFlow.emit(it)
                is Result.Success -> _squadFlow.emit(it)
                is Result.Auth -> _squadFlow.emit(it)
            }
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        updatePointUseCase(updatePointRequest).collect {
            when (it) {
                is Result.Error -> _updatePointFlow.emit(it)
                is Result.Loading -> _updatePointFlow.emit(it)
                is Result.Success -> {
                    it.body!!.data.lastPoint = updatePointRequest.point
                    _updatePointFlow.emit(it)
                }
                is Result.Auth -> _updatePointFlow.emit(it)
            }
        }
    }

    fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        refreshTokenUseCase(refreshToken).collect {
            when (it) {
                is Result.Error -> _refreshTokenFlow.emit(it)
                is Result.Loading -> _refreshTokenFlow.emit(it)
                is Result.Success -> _refreshTokenFlow.emit(it)
                is Result.Auth -> _refreshTokenFlow.emit(it)
            }
        }
    }
}