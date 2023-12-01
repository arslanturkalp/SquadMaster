package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.UnlockLeagueRequest
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.UnlockLeagueResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class UnlockLeagueUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<UnlockLeagueRequest, UnlockLeagueResponse>() {
    override suspend fun getData(params: UnlockLeagueRequest?): Result<UnlockLeagueResponse> = repository.unlockLeague(params!!)
}