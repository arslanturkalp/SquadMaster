package com.umtualgames.squadmaster.data.api

import com.umtualgames.squadmaster.domain.entities.requests.LevelPassRequest
import com.umtualgames.squadmaster.domain.entities.requests.UnlockLeagueRequest
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.item.Player
import com.umtualgames.squadmaster.domain.entities.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RegisterResponse
import com.umtualgames.squadmaster.domain.entities.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.domain.entities.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.domain.entities.responses.roomresponses.CreateRoomResponse
import com.umtualgames.squadmaster.domain.entities.responses.roomresponses.GetRoomsResponse
import com.umtualgames.squadmaster.domain.entities.responses.roomresponses.JoinRoomResponse
import com.umtualgames.squadmaster.domain.entities.responses.roomresponses.LeaveRoomResponse
import com.umtualgames.squadmaster.domain.entities.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.UnlockLeagueResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("League/GetLeagues")
    suspend fun getLeagues(@Query("userID") userID: Int): Response<GetLeaguesResponse>

    @POST("Login/Register")
    suspend fun register(@Body registerRequest: com.umtualgames.squadmaster.domain.entities.requests.RegisterRequest): Response<RegisterResponse>

    @GET("Player/GetFirstElevenBySquadName")
    suspend fun getFirstElevenBySquadName(@Query("squadName") squadName: String): Response<GetFirstElevenBySquadResponse>

    @GET("Player/GetPlayerListBySquadName")
    suspend fun getPlayerListBySquadName(@Query("squadName") squadName: String): Response<List<Player>>

    @GET("Player/GetFirstElevenByRandomSquad")
    suspend fun getFirstElevenByRandomSquad(): Response<GetFirstElevenBySquadResponse>

    @GET("ProjectSettings/GetProjectSettings")
    suspend fun getProjectSettings(): Response<ProjectSettingsResponse>

    @GET("Room/GetRooms")
    suspend fun getRooms(): Response<GetRoomsResponse>

    @POST("Room/CreateRoom")
    suspend fun createRoom(@Query("roomName") roomName: String): Response<CreateRoomResponse>

    @POST("Room/JoinRoom")
    suspend fun joinRoom(): Response<JoinRoomResponse>

    @POST("Room/LeaveRoom")
    suspend fun leaveRoom(@Query("roomName") roomName: String): Response<LeaveRoomResponse>

    @GET("Squad/GetSquadList")
    suspend fun getSquadList(): Response<GetSquadListResponse>

    @GET("Squad/GetSquadListByLeague")
    suspend fun getSquadListByLeague(@Query("leagueID") leagueID: Int, @Query("userID") userID: Int): Response<GetSquadListResponse>

    @POST("UnlockSquadToUser/LevelPass")
    suspend fun levelPass(@Body levelPassRequest: LevelPassRequest): Response<LevelPassResponse>

    @POST("UnlockSquadToUser/LevelPassWithoutPoint")
    suspend fun unlockLeague(@Body levelPassRequest: UnlockLeagueRequest): Response<UnlockLeagueResponse>

    @GET("UserPoint/GetUserPoint")
    suspend fun getUserPoint(@Query("userID") userID: Int): Response<UserPointResponse>

    @POST("UserPoint/UpdatePoint")
    suspend fun updatePoint(@Body updatePointRequest: UpdatePointRequest): Response<UserPointResponse>

    @GET("UserPoint/GetRankList")
    suspend fun getRankList(): Response<GetRankListResponse>
}