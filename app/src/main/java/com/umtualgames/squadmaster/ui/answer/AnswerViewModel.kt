package com.umtualgames.squadmaster.ui.answer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.responses.item.Token
import com.umtualgames.squadmaster.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnswerViewModel @Inject constructor(private val repository: Repository): BaseViewModel() {

    private val viewState = MutableLiveData<AnswerViewState>()
    val getViewState: LiveData<AnswerViewState> = viewState

    private fun refreshTokenLogin(refreshToken: String) = viewModelScope.launch {
        viewState.postValue(AnswerViewState.LoadingState)
        repository.refreshTokenLogin(refreshToken).let {
            when {
                it.isSuccessful -> viewState.postValue(AnswerViewState.RefreshState(it.body()!!.data.token))
                else -> viewState.postValue(AnswerViewState.ErrorState(it.message()))
            }
        }
    }
}

sealed class AnswerViewState {
    object LoadingState : AnswerViewState()
    data class ErrorState(val message: String) : AnswerViewState()
    data class WarningState(val message: String?) : AnswerViewState()
    data class RefreshState(val response: Token) : AnswerViewState()
}