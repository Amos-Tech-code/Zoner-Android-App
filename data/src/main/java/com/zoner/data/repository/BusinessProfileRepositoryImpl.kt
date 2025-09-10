package com.zoner.data.repository

import com.zoner.domain.ResultWrapper
import com.zoner.domain.model.request.CreateBusinessProfile
import com.zoner.domain.model.response.LoginResponse
import com.zoner.domain.network.NetworkService
import com.zoner.domain.repository.BusinessProfileRepository

/**
 * BusinessProfileRepositoryImpl.kt
 */
class BusinessProfileRepositoryImpl(private val networkService: NetworkService) : BusinessProfileRepository {
    override suspend fun createBusinessProfile(businessProfile: CreateBusinessProfile): ResultWrapper<LoginResponse> {
        return networkService.createBusinessProfile(businessProfile)
    }


}