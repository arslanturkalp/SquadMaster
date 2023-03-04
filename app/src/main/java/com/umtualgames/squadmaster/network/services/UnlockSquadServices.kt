package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.requests.LevelPassRequest
import com.umtualgames.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface UnlockSquadServices {

    @POST("UnlockSquadToUser/LevelPass")
    fun levelPass(@Body levelPassRequest: LevelPassRequest): Single<LevelPassResponse>
}