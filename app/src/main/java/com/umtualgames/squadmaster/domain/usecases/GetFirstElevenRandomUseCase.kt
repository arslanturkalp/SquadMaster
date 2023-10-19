package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetFirstElevenRandomUseCase @Inject constructor(private val repository: RepositoryNew): BaseUseCase<Any, GetFirstElevenBySquadResponse>() {
    override suspend fun getData(params: Any?) = repository.getFirstElevenByRandomSquad()
}