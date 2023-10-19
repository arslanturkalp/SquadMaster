package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.requests.LevelPassRequest
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class LevelPassUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<LevelPassRequest, LevelPassResponse>() {
    override suspend fun getData(params: LevelPassRequest?) = repository.levelPass(params!!)
}