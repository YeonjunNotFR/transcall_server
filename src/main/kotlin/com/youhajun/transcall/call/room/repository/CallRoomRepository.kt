package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface CallRoomRepository : CoroutineCrudRepository<CallRoom, UUID>, CallRoomRepositoryCustom