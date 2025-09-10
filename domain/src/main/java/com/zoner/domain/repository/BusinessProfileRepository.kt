package com.zoner.domain.repository

import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.request.CreateBusinessProfile
import com.zoner.domain.model.response.LoginResponse

/**
 * BusinessProfileRepository.kt
 */
interface BusinessProfileRepository {

    suspend fun createBusinessProfile(businessProfile: CreateBusinessProfile): ResultWrapper<LoginResponse>

}