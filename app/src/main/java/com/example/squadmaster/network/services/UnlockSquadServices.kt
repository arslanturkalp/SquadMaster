package com.example.squadmaster.network.services

import com.example.squadmaster.network.requests.LevelPassRequest
import com.example.squadmaster.network.responses.unlocksquadresponses.LevelPassResponse
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface UnlockSquadServices {

    @POST("UnlockSquadToUser/LevelPass")
    fun levelPass(@Body levelPassRequest: LevelPassRequest): Single<LevelPassResponse>
}