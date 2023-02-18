package com.example.squadmaster.ui.score

import BaseViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.squadmaster.data.enums.Status
import com.example.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.example.squadmaster.network.responses.userpointresponses.UserPointResponse
import com.example.squadmaster.ui.home.HomeViewState
import com.example.squadmaster.utils.applyThreads

class ScoreViewModel: BaseViewModel() {
    
    private val viewState = MutableLiveData<ScoreViewState>()
    val getViewState: LiveData<ScoreViewState> = viewState

    fun getRankList() {
        compositeDisposable.addAll(
            remoteDataSource
                .getRankList()
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(ScoreViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(ScoreViewState.SuccessState(response))
                        }
                        Status.ERROR -> viewState.postValue(ScoreViewState.ErrorState(it.message!!))
                    }
                }
        )
    }

    fun getUserPoint(userId: Int) {
        compositeDisposable.addAll(
            remoteDataSource
                .getPoint(userId)
                .applyThreads()
                .subscribe {
                    when (it.status) {
                        Status.LOADING -> viewState.postValue(ScoreViewState.LoadingState)
                        Status.SUCCESS -> {
                            val response = it.data!!
                            viewState.postValue(ScoreViewState.UserPointState(response))
                        }
                        Status.ERROR -> viewState.postValue(ScoreViewState.ErrorState(it.message!!))
                    }
                }
        )
    }
}


sealed class ScoreViewState {
    object LoadingState : ScoreViewState()
    data class SuccessState(val response: GetRankListResponse) : ScoreViewState()
    data class ErrorState(val message: String) : ScoreViewState()
    data class WarningState(val message: String?) : ScoreViewState()
    data class UserPointState(val response: UserPointResponse) : ScoreViewState()
}