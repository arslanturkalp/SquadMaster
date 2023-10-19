package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetRankListUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<Any, GetRankListResponse>() {
    override suspend fun getData(params: Any?) = repository.getRankList()
}