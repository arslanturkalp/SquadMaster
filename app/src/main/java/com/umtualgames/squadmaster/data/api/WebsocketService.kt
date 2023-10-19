package com.umtualgames.squadmaster.data.api

import com.umtualgames.squadmaster.domain.entities.responses.item.SuccessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebsocketService {

    @GET("getAvailableRoomCount")
    suspend fun getAvailableRoomCount(): Response<Int>

    @POST("createRoom")
    suspend fun createRoom(@Body roomRequest: com.umtualgames.squadmaster.domain.entities.requests.RoomRequest): Response<SuccessResponse>

    @POST("joinRoom")
    suspend fun joinRoom(@Body roomRequest: com.umtualgames.squadmaster.domain.entities.requests.RoomRequest): Response<SuccessResponse>

    @GET("getRooms")
    suspend fun getRooms(): Response<List<com.umtualgames.squadmaster.domain.entities.requests.RoomRequest>>
}