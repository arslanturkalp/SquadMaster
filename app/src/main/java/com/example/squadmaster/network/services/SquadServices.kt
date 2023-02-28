package com.example.squadmaster.network.services

import com.example.squadmaster.network.responses.item.Club
import com.example.squadmaster.network.responses.squadresponses.GetSquadListResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SquadServices {

    @GET("Squad/GetSquadList")
    fun getSquadList(): Single<GetSquadListResponse>

    @GET("Squad/GetSquadListByLeague")
    fun getSquadListByLeague(@Query("leagueID") leagueID: Int, @Query("userID") userID: Int): Single<GetSquadListResponse>

}