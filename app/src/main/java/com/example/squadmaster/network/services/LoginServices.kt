package com.example.squadmaster.network.services

import com.example.squadmaster.network.requests.LoginRequest
import com.example.squadmaster.network.requests.RegisterRequest
import com.example.squadmaster.network.responses.loginresponses.LoginResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginServices {

    @POST("Login/SignIn")
    fun login(@Body loginRequest: LoginRequest): Single<LoginResponse>

    @POST("Login/Register")
    fun register(@Body registerRequest: RegisterRequest): Single<Boolean>

    @GET("Login/RefreshTokenLogin")
    fun refreshTokenLogin(@Query("refreshToken") refreshToken: String): Single<LoginResponse>
}