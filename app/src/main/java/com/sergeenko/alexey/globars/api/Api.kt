package com.sergeenko.alexey.globars.api

import com.sergeenko.alexey.globars.dataClasses.LogResult
import com.sergeenko.alexey.globars.dataClasses.TrackingSessionsResult
import com.sergeenko.alexey.globars.dataClasses.TransportObjects
import com.sergeenko.alexey.globars.dataClasses.User
import retrofit2.Call
import retrofit2.http.*


interface GlobarsApiService {

    @POST("auth/login")
    fun auth(@Body username: User): Call<LogResult>


    @GET("tracking/sessions")
    fun getTrackingSessions(): Call<TrackingSessionsResult>

    @GET("tracking/{sessionId}/units")
    fun getObjects(
        @Path("sessionId") sessionId:String
    ): Call<TransportObjects>



}
