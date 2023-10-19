package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class GetPointUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<Int, UserPointResponse>() {
    override suspend fun getData(params: Int?) = repository.getUserPoint(params!!)
}