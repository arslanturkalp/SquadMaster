package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.requests.RegisterRequest
import com.umtualgames.squadmaster.network.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.RegisterResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginServices {

    @POST("Login/SignIn")
    fun login(@Body loginRequest: LoginRequest): Single<LoginResponse>

    @POST("Login/Register")
    fun register(@Body registerRequest: RegisterRequest): Single<RegisterResponse>

    @GET("Login/RefreshTokenLogin")
    fun refreshTokenLogin(@Query("refreshToken") refreshToken: String): Single<RefreshTokenResponse>
}