package com.umtualgames.squadmaster.domain.usecases

import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: RepositoryNew) : BaseUseCase<LoginRequest, LoginResponse>() {
    override suspend fun getData(params: LoginRequest?) = repository.login(params!!)
}