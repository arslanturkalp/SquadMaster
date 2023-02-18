package com.example.squadmaster.network.services

import com.example.squadmaster.network.responses.leagueresponses.GetLeaguesResponse
import io.reactivex.Single
import retrofit2.http.GET

interface LeagueServices {

    @GET("League/GetLeagues")
    fun getLeagues(): Single<GetLeaguesResponse>
}