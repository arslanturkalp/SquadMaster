package com.umtualgames.squadmaster.ui.online.compare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.application.SessionManager
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.domain.entities.responses.item.Token
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompareViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private val viewState = MutableLiveData<CompareViewState>()
    val getViewState: LiveData<CompareViewState> = viewState

    fun updatePoint(updatePointRequest: com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest) = viewModelScope.launch {
        viewState.postValue(CompareViewState.UserPointLoadingState)
        repository.updatePoint(updatePointRequest).let {
            if (it.isSuccessful) viewState.postValue(CompareViewState.UpdateState) else refreshTokenLogin(SessionManager.getRefreshToken())
        }
    }

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(CompareViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> viewState.postValue(CompareViewState.RefreshState(it.body()!!.data.token))
                it.code() != 200 -> viewState.postValue(CompareViewState.ReturnSplashState)
                else -> viewState.postValue(CompareViewState.ErrorState(it.message()))
            }
        }
    }
}

sealed class CompareViewState {
    object LoadingState : CompareViewState()
    object UserPointLoadingState : CompareViewState()
    object ReturnSplashState : CompareViewState()
    object UpdateState : CompareViewState()
    data class RefreshState(val response: Token) : CompareViewState()
    data class ErrorState(val message: String) : CompareViewState()
    data class WarningState(val message: String?) : CompareViewState()
}