package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.requests.RegisterRequest
import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.network.responses.loginresponses.RegisterResponse
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.network.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.network.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("League/GetLeagues")
    suspend fun getLeagues(@Query("userID") userID: Int): Response<GetLeaguesResponse>

    @POST("Login/Register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @GET("Player/GetFirstElevenBySquadName")
    suspend fun getFirstElevenBySquadName(@Query("squadName") squadName: String) : Response<GetFirstElevenBySquadResponse>

    @GET("Player/GetPlayerListBySquadName")
    suspend fun getPlayerListBySquadName(@Query("squadName") squadName: String) : Response<List<Player>>

    @GET("Player/GetFirstElevenByRandomSquad")
    suspend fun getFirstElevenByRandomSquad() : Response<GetFirstElevenBySquadResponse>

    @GET("ProjectSettings/GetProjectSettings")
    suspend fun getProjectSettings() : Response<ProjectSettingsResponse>

    @GET("Squad/GetSquadList")
    suspend fun getSquadList(): Response<GetSquadListResponse>

    @GET("Squad/GetSquadListByLeague")
    suspend fun getSquadListByLeague(@Query("leagueID") leagueID: Int, @Query("userID") userID: Int): Response<GetSquadListResponse>

    @POST("UnlockSquadToUser/LevelPass")
    suspend fun levelPass(@Body levelPassRequest: LevelPassRequest): Response<LevelPassResponse>

    @GET("UserPoint/GetUserPoint")
    suspend fun getUserPoint(@Query("userID") userID: Int): Response<UserPointResponse>

    @POST("UserPoint/UpdatePoint")
    suspend fun updatePoint(@Body updatePointRequest: UpdatePointRequest): Response<UserPointResponse>

    @GET("UserPoint/GetRankList")
    suspend fun getRankList(): Response<GetRankListResponse>
}