package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class UpdatePointUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<UpdatePointRequest, UserPointResponse>() {
    override suspend fun getData(params: UpdatePointRequest?) = repository.updatePoint(params!!)
}