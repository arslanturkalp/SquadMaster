package com.umtualgames.squadmaster.domain.repository

import com.umtualgames.squadmaster.data.entities.models.Result
import com.umtualgames.squadmaster.domain.entities.requests.LevelPassRequest
import com.umtualgames.squadmaster.domain.entities.requests.LoginRequest
import com.umtualgames.squadmaster.domain.entities.requests.UpdatePointRequest
import com.umtualgames.squadmaster.domain.entities.responses.leagueresponses.GetLeaguesResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.LoginResponse
import com.umtualgames.squadmaster.domain.entities.responses.loginresponses.RefreshTokenResponse
import com.umtualgames.squadmaster.domain.entities.responses.playerresponses.GetFirstElevenBySquadResponse
import com.umtualgames.squadmaster.domain.entities.responses.projectsettingsresponses.ProjectSettingsResponse
import com.umtualgames.squadmaster.domain.entities.responses.squadresponses.GetSquadListResponse
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.LevelPassResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.domain.entities.responses.userpointresponses.UserPointResponse

interface RepositoryNew {

    suspend fun getSquadListByLeague(leagueID: Int, userID: Int): Result<GetSquadListResponse>

    suspend fun getFirstElevenBySquadName(squadName: String): Result<GetFirstElevenBySquadResponse>

    suspend fun levelPass(levelPassRequest: LevelPassRequest): Result<LevelPassResponse>

    suspend fun updatePoint(updatePointRequest: UpdatePointRequest): Result<UserPointResponse>

    suspend fun getUserPoint(userID: Int): Result<UserPointResponse>

    suspend fun refreshTokenLogin(refreshToken: String): Result<RefreshTokenResponse>

    suspend fun getFirstElevenByRandomSquad(): Result<GetFirstElevenBySquadResponse>

    suspend fun getLeagues(userID: Int): Result<GetLeaguesResponse>

    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>

    suspend fun getRankList(): Result<GetRankListResponse>

    suspend fun getProjectSettings(): Result<ProjectSettingsResponse>
}