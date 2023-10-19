package com.umtualgames.squadmaster.di

import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.requests.RegisterRequest
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.data.api.ApiService
import com.umtualgames.squadmaster.data.api.ForTokenApiService

class Repository(private val apiService: ApiService, private val forTokenApiService: ForTokenApiService) {

    suspend fun register(registerRequest: RegisterRequest) = apiService.register(registerRequest)

    suspend fun updatePoint(updatePointRequest: UpdatePointRequest) = apiService.updatePoint(updatePointRequest)

    suspend fun login(loginRequest: LoginRequest) = forTokenApiService.login(loginRequest)

    suspend fun refreshTokenLogin(refreshToken: String) = forTokenApiService.refreshTokenLogin(refreshToken)

    suspend fun getRooms() = apiService.getRooms()

    suspend fun createRoom(roomName: String) = apiService.createRoom(roomName)

    suspend fun joinRoom() = apiService.joinRoom()

    suspend fun leaveRoom(roomName: String) = apiService.leaveRoom(roomName)
}