package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CallParticipantRepository : CoroutineCrudRepository<CallParticipant, UUID>, CallParticipantRepositoryCustom