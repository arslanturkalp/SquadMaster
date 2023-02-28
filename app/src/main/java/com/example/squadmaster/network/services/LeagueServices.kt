package com.example.squadmaster.network.services

import com.example.squadmaster.network.responses.leagueresponses.GetLeaguesResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface LeagueServices {

    @GET("League/GetLeagues")
    fun getLeagues(@Query("userID") userID: Int): Single<GetLeaguesResponse>
}