package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetFirstElevenForSquadUseCase @Inject constructor(private val repository: RepositoryNew): BaseUseCase<String, GetFirstElevenBySquadResponse>() {
    override suspend fun getData(params: String?) = repository.getFirstElevenBySquadName(params!!)
}