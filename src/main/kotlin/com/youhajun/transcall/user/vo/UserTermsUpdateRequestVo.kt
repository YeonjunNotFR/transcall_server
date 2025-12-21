package com.youhajun.transcall.user.vo

data class UserTermsUpdateRequestVo(
    val isPushEnabled: Boolean? = null,
    val isMarketingAgreed: Boolean? = null,
    val isPrivacyAgreed: Boolean? = null,
    val isTermsAgreed: Boolean? = null,
)
