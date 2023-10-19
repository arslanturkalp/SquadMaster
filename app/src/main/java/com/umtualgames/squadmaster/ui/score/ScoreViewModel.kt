package com.umtualgames.squadmaster.ui.score

import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.usecases.GetPointUseCase
import com.umtualgames.squadmaster.domain.usecases.GetRankListUseCase
import com.umtualgames.squadmaster.domain.usecases.RefreshTokenUseCase
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel @Inject constructor(
    private val getRankListUseCase: GetRankListUseCase,
    private val getPointUseCase: GetPointUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : BaseViewModel() {

    private val _rankFlow: MutableStateFlow<Result<GetRankListResponse>> = MutableStateFlow(Result.Loading())
    val rankFlow: StateFlow<Result<GetRankListResponse>> = _rankFlow

    private val _getPointFlow: MutableStateFlow<Result<UserPointResponse>> = MutableStateFlow(Result.Loading())
    val getPointFlow: StateFlow<Result<UserPointResponse>> = _getPointFlow

    private val _refreshTokenFlow: MutableStateFlow<Result<RefreshTokenResponse>> = MutableStateFlow(Result.Loading())
    val refreshTokenFlow: StateFlow<Result<RefreshTokenResponse>> = _refreshTokenFlow

    fun getRankList() = viewModelScope.launch {
        getRankListUseCase().collect {
            when (it) {
                is Result.Error -> _rankFlow.emit(it)
                is Result.Loading -> _rankFlow.emit(it)
                is Result.Success -> _rankFlow.emit(it)
                is Result.Auth -> _rankFlow.emit(it)
            }
        }
    }

    fun getUserPoint(userId: Int) = viewModelScope.launch {
        getPointUseCase(userId).collect {
            when (it) {
                is Result.Error -> _getPointFlow.emit(it)
                is Result.Loading -> _getPointFlow.emit(it)
                is Result.Success -> _getPointFlow.emit(it)
                is Result.Auth -> _getPointFlow.emit(it)
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