package com.zoner.domain.usecase

import com.zoner.domain.model.StatusGroup
import com.zoner.domain.model.UserStatus
import com.zoner.domain.repository.StatusRepository
import kotlinx.coroutines.flow.Flow

data class StatusItemsUseCases(
    val getUserStatus: GetUserStatus,
    val saveUserStatus: SaveUserStatus
) {
    class GetUserStatus(private val statusRepository: StatusRepository) {
        suspend operator fun invoke() : Flow<List<StatusGroup>> =
            statusRepository.getStatusGroups()
    }

    class SaveUserStatus(private val statusRepository: StatusRepository) {
        suspend operator fun invoke(status: UserStatus) =
            statusRepository.saveStatus(status)
    }
}