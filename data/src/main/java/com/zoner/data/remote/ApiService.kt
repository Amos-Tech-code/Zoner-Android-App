package com.zoner.data.remote

import retrofit2.http.POST

interface ApiService {

    @POST("")
    suspend fun login()

}