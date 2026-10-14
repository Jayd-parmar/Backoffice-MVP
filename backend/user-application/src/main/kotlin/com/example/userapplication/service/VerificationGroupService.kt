package com.example.userapplication.service

import com.example.userapplication.model.VerificationGroup
import com.example.userapplication.model.VerificationGroupsDocument
import com.example.userapplication.repository.VerificationGroupsRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class VerificationGroupService(
    private val repository: VerificationGroupsRepository,
) {
    fun find(applicationId: ObjectId): List<VerificationGroup>? {
        return repository.findByApplicationId(applicationId)?.groups
    }

    fun save(applicationId: ObjectId, groups: List<VerificationGroup>) {
        val existing = repository.findByApplicationId(applicationId)
        repository.save(
            VerificationGroupsDocument(
                id = existing?.id ?: ObjectId(),
                applicationId = applicationId,
                groups = groups,
            )
        )
    }
}
