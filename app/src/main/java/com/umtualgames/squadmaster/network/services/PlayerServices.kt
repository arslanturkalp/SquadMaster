package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.responses.item.Player
import com.umtualgames.squadmaster.network.responses.playerresponses.GetFirstElevenBySquadResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface PlayerServices {

    @GET("Player/GetFirstElevenBySquadName")
    fun getFirstElevenBySquadName(@Query("squadName") squadName: String) : Single<GetFirstElevenBySquadResponse>

    @GET("Player/GetPlayerListBySquadName")
    fun getPlayerListBySquadName(@Query("squadName") squadName: String) : Single<List<Player>>

    @GET("Player/GetFirstElevenByRandomSquad")
    fun getFirstElevenByRandomSquad() : Single<GetFirstElevenBySquadResponse>
}