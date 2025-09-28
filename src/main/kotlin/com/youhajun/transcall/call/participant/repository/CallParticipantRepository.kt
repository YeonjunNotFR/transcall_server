package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface CallParticipantRepository : CoroutineCrudRepository<CallParticipant, UUID>, CallParticipantRepositoryCustom