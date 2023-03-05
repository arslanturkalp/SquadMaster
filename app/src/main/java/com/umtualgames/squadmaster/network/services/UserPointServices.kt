package com.umtualgames.squadmaster.network.services

import com.umtualgames.squadmaster.network.requests.UpdatePointRequest
import com.umtualgames.squadmaster.network.responses.userpointresponses.GetRankListResponse
import com.umtualgames.squadmaster.network.responses.userpointresponses.UserPointResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserPointServices {

    @GET("UserPoint/GetUserPoint")
    fun getUserPoint(@Query("userID") userID: Int): Single<UserPointResponse>

    @POST("UserPoint/UpdatePoint")
    fun updatePoint(@Body updatePointRequest: UpdatePointRequest): Single<UserPointResponse>

    @GET("UserPoint/GetRankList")
    fun getRankList(): Single<GetRankListResponse>
}