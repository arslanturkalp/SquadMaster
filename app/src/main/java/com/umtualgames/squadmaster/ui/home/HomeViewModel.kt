package com.umtualgames.squadmaster.ui.home

import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.usecases.GetLeaguesUseCase
import com.umtualgames.squadmaster.domain.usecases.GetPointUseCase
import com.umtualgames.squadmaster.domain.usecases.RefreshTokenUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPointUseCase: GetPointUseCase,
    private val getLeaguesUseCase: GetLeaguesUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : BaseViewModel() {

    private val _getPointFlow: MutableStateFlow<Result<UserPointResponse>> = MutableStateFlow(Result.Loading())
    val getPointFlow: StateFlow<Result<UserPointResponse>> = _getPointFlow

    private val _getLeaguesFlow: MutableStateFlow<Result<GetLeaguesResponse>> = MutableStateFlow(Result.Loading())
    val getLeaguesFlow: StateFlow<Result<GetLeaguesResponse>> = _getLeaguesFlow

    private val _refreshTokenFlow: MutableStateFlow<Result<RefreshTokenResponse>> = MutableStateFlow(Result.Loading())
    val refreshTokenFlow: StateFlow<Result<RefreshTokenResponse>> = _refreshTokenFlow

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