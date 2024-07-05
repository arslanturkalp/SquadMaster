package com.umtualgames.squadmaster.data.api

import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ForTokenApiService {

    @POST("Login/SignIn")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("Login/RefreshTokenLogin")
    suspend fun refreshTokenLogin(@Query("refreshToken") refreshToken: String): Response<RefreshTokenResponse>
}