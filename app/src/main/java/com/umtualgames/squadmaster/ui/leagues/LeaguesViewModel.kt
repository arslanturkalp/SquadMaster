package com.umtualgames.squadmaster.ui.leagues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.umtualgames.squadmaster.di.Repository
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.League
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
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

    /*Old GetLeagues
fun getLeagues() {
    compositeDisposable.addAll(
        remoteDataSource
            .getLeagues(getUserID())
            .applyThreads()
            .subscribe {
                when (it.status) {
                    Status.LOADING -> viewState.postValue(LeaguesViewState.LoadingState)
                    Status.SUCCESS -> {
                        val response = it.data!!
                        viewState.postValue(LeaguesViewState.SuccessState(response.data))
                    }
                    Status.ERROR -> { viewState.postValue(LeaguesViewState.ErrorState(it.message!!)) }
                }
            }
    )
}*/

    /* Old UpdatePoint
    fun updatePoint(updatePointRequest: UpdatePointRequest) {
        compositeDisposable.addAll(
            remoteDataSource
                .updatePoint(updatePointRequest)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(LeaguesViewState.UserPointLoadingState)
                        Status.SUCCESS -> {
                            viewState.postValue(LeaguesViewState.UpdateState)
                        }
                        Status.ERROR -> viewState.postValue(LeaguesViewState.ErrorState(it.message!!))
                    }
                }
        )
    }*/
}


sealed class LeaguesViewState {
    object LoadingState : LeaguesViewState()
    object UserPointLoadingState : LeaguesViewState()
    object UpdateState : LeaguesViewState()
    data class SuccessState(val response: List<League>) : LeaguesViewState()
    data class ErrorState(val message: String) : LeaguesViewState()
    data class WarningState(val message: String?) : LeaguesViewState()
    data class RefreshState(val response: LoginResponse) : LeaguesViewState()
}