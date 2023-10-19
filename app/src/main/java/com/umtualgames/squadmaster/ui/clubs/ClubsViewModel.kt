package com.umtualgames.squadmaster.ui.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.domain.usecases.GetSquadListUseCase
import com.umtualgames.squadmaster.domain.usecases.RefreshTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClubsViewModel @Inject constructor(
    private val getSquadListUseCase: GetSquadListUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : ViewModel() {

    private val _squadListFlow: MutableStateFlow<Result<GetSquadListResponse>> = MutableStateFlow(Result.Loading())
    val squadListFlow: StateFlow<Result<GetSquadListResponse>> = _squadListFlow

    private val _refreshTokenFlow: MutableStateFlow<Result<RefreshTokenResponse>> = MutableStateFlow(Result.Loading())
    val refreshTokenFlow: StateFlow<Result<RefreshTokenResponse>> = _refreshTokenFlow

    fun getSquadListByLeague(leagueID: Int, userID: Int, levelPass: Boolean = false) = viewModelScope.launch {
        getSquadListUseCase(arrayListOf(leagueID, userID, levelPass)).collect {
            when (it) {
                is Result.Error -> _squadListFlow.emit(it)
                is Result.Loading -> _squadListFlow.emit(it)
                is Result.Success -> {
                    it.body!!.levelPass = levelPass
                    _squadListFlow.emit(it)
                }
                is Result.Auth -> _squadListFlow.emit(it)
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