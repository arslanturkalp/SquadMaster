package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetSquadListUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<ArrayList<Any>, GetSquadListResponse>() {
    override suspend fun getData(params: ArrayList<Any>?) = repository.getSquadListByLeague(params!![0].toString().toInt(), params[1].toString().toInt())
}