package com.youhajun.transcall.janus.dto.plugin

data class JanusPluginData<T : Any>(
    val plugin: String,
    val data: T
)