package com.umtualgames.squadmaster.data.repository

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
import com.umtualgames.squadmaster.domain.repository.RepositoryNew
import com.umtualgames.squadmaster.data.api.ApiService
import com.umtualgames.squadmaster.data.api.ForTokenApiService
import com.umtualgames.squadmaster.domain.entities.requests.UnlockLeagueRequest
import com.umtualgames.squadmaster.domain.entities.responses.unlocksquadresponses.UnlockLeagueResponse
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val apiService: ApiService, private val forTokenApiService: ForTokenApiService) : RepositoryNew {

    override suspend fun getSquadListByLeague(leagueID: Int, userID: Int): Result<GetSquadListResponse> {
        val response = apiService.getSquadListByLeague(leagueID, userID)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getFirstElevenBySquadName(squadName: String): Result<GetFirstElevenBySquadResponse> {
        val response = apiService.getFirstElevenBySquadName(squadName)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun levelPass(levelPassRequest: LevelPassRequest): Result<LevelPassResponse> {
        val response = apiService.levelPass(levelPassRequest)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun unlockLeague(unlockLeagueRequest: UnlockLeagueRequest): Result<UnlockLeagueResponse> {
        val response = apiService.unlockLeague(unlockLeagueRequest)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun updatePoint(updatePointRequest: UpdatePointRequest): Result<UserPointResponse> {
        val response = apiService.updatePoint(updatePointRequest)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getUserPoint(userID: Int): Result<UserPointResponse> {
        val response = apiService.getUserPoint(userID)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else if (response.code() == 401) {
                Result.Auth(response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun refreshTokenLogin(refreshToken: String): Result<RefreshTokenResponse> {
        val response = forTokenApiService.refreshTokenLogin(refreshToken)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getFirstElevenByRandomSquad(): Result<GetFirstElevenBySquadResponse> {
        val response = apiService.getFirstElevenByRandomSquad()
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getLeagues(userID: Int): Result<GetLeaguesResponse> {
        val response = apiService.getLeagues(userID)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        val response = forTokenApiService.login(loginRequest)
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getRankList(): Result<GetRankListResponse> {
        val response = apiService.getRankList()
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }

    override suspend fun getProjectSettings(): Result<ProjectSettingsResponse> {
        val response = apiService.getProjectSettings()
        return try {
            return if (response.isSuccessful) {
                Result.Success(response.body(), response.code(), response.message())
            } else Result.Error(response.code(), response.message())
        } catch (e: Exception) {
            Result.Error(response.code(), e.message)
        }
    }
}