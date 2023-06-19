package com.umtualgames.squadmaster.di

import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.LoginRequest
import com.umtualgames.squadmaster.network.requests.RegisterRequest
import com.umtualgames.squadmaster.network.requests.RoomRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.services.ApiService
import com.umtualgames.squadmaster.network.services.ForTokenApiService
import com.umtualgames.squadmaster.network.services.WebsocketService

class Repository(private val apiService: ApiService, private val forTokenApiService: ForTokenApiService, private val websocketService: WebsocketService) {

    suspend fun getLeagues(userID: Int) = apiService.getLeagues(userID)

    suspend fun register(registerRequest: RegisterRequest) = apiService.register(registerRequest)

    suspend fun getFirstElevenBySquadName(squadName: String) = apiService.getFirstElevenBySquadName(squadName)

    suspend fun getPlayerListBySquadName(squadName: String) = apiService.getPlayerListBySquadName(squadName)

    suspend fun getFirstElevenByRandomSquad() = apiService.getFirstElevenByRandomSquad()

    suspend fun getProjectSettings() = apiService.getProjectSettings()

    suspend fun getSquadList() = apiService.getSquadList()

    suspend fun getSquadListByLeague(leagueID: Int, userID: Int) = apiService.getSquadListByLeague(leagueID, userID)

    suspend fun levelPass(levelPassRequest: LevelPassRequest) = apiService.levelPass(levelPassRequest)

    suspend fun getUserPoint(userID: Int) = apiService.getUserPoint(userID)

    suspend fun updatePoint(updatePointRequest: UpdatePointRequest) = apiService.updatePoint(updatePointRequest)

    suspend fun getRankList() = apiService.getRankList()

    suspend fun login(loginRequest: LoginRequest) = forTokenApiService.login(loginRequest)

    suspend fun refreshTokenLogin(refreshToken: String) = forTokenApiService.refreshTokenLogin(refreshToken)

    suspend fun getRooms() = apiService.getRooms()

    suspend fun createRoom(roomName: String) = apiService.createRoom(roomName)

    suspend fun joinRoom() = apiService.joinRoom()

    suspend fun leaveRoom(roomName: String) = apiService.leaveRoom(roomName)

}