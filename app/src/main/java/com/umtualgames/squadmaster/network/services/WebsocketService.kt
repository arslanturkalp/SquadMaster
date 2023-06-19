package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.requests.RoomRequest
import com.umtualgames.squadmaster.network.responses.item.SuccessResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WebsocketService {

    @GET("getAvailableRoomCount")
    suspend fun getAvailableRoomCount(): Response<Int>

    @POST("createRoom")
    suspend fun createRoom(@Body roomRequest: RoomRequest): Response<SuccessResponse>

    @POST("joinRoom")
    suspend fun joinRoom(@Body roomRequest: RoomRequest): Response<SuccessResponse>

    @GET("getRooms")
    suspend fun getRooms(): Response<List<RoomRequest>>
}