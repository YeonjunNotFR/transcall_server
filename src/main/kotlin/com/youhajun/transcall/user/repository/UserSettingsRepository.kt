package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserSettings
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserSettingsRepository : CoroutineCrudRepository<UserSettings, UUID>
