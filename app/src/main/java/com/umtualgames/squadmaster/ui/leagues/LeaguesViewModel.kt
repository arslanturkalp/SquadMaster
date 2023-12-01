package com.umtualgames.squadmaster.ui.leagues

import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.UnlockLeagueRequest
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.UnlockLeagueResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.usecases.GetLeaguesUseCase
import com.umtualgames.squadmaster.domain.usecases.GetPointUseCase
import com.umtualgames.squadmaster.domain.usecases.RefreshTokenUseCase
import com.umtualgames.squadmaster.domain.usecases.UnlockLeagueUseCase
import com.umtualgames.squadmaster.domain.usecases.UpdatePointUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaguesViewModel @Inject constructor(
    private val getLeaguesUseCase: GetLeaguesUseCase,
    private val updatePointUseCase: UpdatePointUseCase,
    private val getPointUseCase: GetPointUseCase,
    private val unlockLeagueUseCase: UnlockLeagueUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : BaseViewModel() {

    private val _getLeaguesFlow: MutableStateFlow<Result<GetLeaguesResponse>> = MutableStateFlow(Result.Loading())
    val getLeaguesFlow: StateFlow<Result<GetLeaguesResponse>> = _getLeaguesFlow

    private val _updatePointFlow: MutableStateFlow<Result<UserPointResponse>> = MutableStateFlow(Result.Loading())
    val updatePointFlow: StateFlow<Result<UserPointResponse>> = _updatePointFlow

    private val _getPointFlow: MutableStateFlow<Result<UserPointResponse>> = MutableStateFlow(Result.Loading())
    val getPointFlow: StateFlow<Result<UserPointResponse>> = _getPointFlow

    private val _unlockLeagueFlow: MutableStateFlow<Result<UnlockLeagueResponse>> = MutableStateFlow(Result.Loading())
    val unlockLeagueFlow: StateFlow<Result<UnlockLeagueResponse>> = _unlockLeagueFlow

    private val _refreshTokenFlow: MutableStateFlow<Result<RefreshTokenResponse>> = MutableStateFlow(Result.Loading())
    val refreshTokenFlow: StateFlow<Result<RefreshTokenResponse>> = _refreshTokenFlow

    fun getLeagues(userID: Int) = viewModelScope.launch {
        getLeaguesUseCase(userID).collect {
            when (it) {
                is Result.Error -> _getLeaguesFlow.emit(it)
                is Result.Loading -> _getLeaguesFlow.emit(it)
                is Result.Success -> _getLeaguesFlow.emit(it)
                is Result.Auth -> _getLeaguesFlow.emit(it)
            }
        }
    }

    fun updatePoint(updatePointRequest: UpdatePointRequest) = viewModelScope.launch {
        updatePointUseCase(updatePointRequest).collect {
            when (it) {
                is Result.Error -> _updatePointFlow.emit(it)
                is Result.Loading -> _updatePointFlow.emit(it)
                is Result.Success -> _updatePointFlow.emit(it)
                is Result.Auth -> _updatePointFlow.emit(it)
            }
        }
    }

    fun getUserPoint(userID: Int) = viewModelScope.launch {
        getPointUseCase(userID).collect {
            when (it) {
                is Result.Error -> _getPointFlow.emit(it)
                is Result.Loading -> _getPointFlow.emit(it)
                is Result.Success -> _getPointFlow.emit(it)
                is Result.Auth -> _getPointFlow.emit(it)
            }
        }
    }

    fun unlockLeague(userID: Int, leagueID: Int) = viewModelScope.launch {
        unlockLeagueUseCase(UnlockLeagueRequest(userID, leagueID)).collect{
            when (it) {
                is Result.Error -> _unlockLeagueFlow.emit(it)
                is Result.Loading -> _unlockLeagueFlow.emit(it)
                is Result.Success -> _unlockLeagueFlow.emit(it)
                is Result.Auth -> _unlockLeagueFlow.emit(it)
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