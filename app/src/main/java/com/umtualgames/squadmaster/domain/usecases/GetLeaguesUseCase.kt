package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetLeaguesUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<Int, GetLeaguesResponse>() {
    override suspend fun getData(params: Int?): Result<GetLeaguesResponse> = repository.getLeagues(params!!)
}