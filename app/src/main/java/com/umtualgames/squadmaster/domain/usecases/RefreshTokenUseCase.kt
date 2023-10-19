package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(private val repository: RepositoryNew): BaseUseCase<String, RefreshTokenResponse>() {
    override suspend fun getData(params: String?) = repository.refreshTokenLogin(params!!)
}