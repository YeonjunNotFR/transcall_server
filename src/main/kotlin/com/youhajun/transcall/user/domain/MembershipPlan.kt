package com.youhajun.transcall.user.domain

enum class MembershipPlan(val string: String) {
    Free("free"),
    Pro("pro"),
    Premium("premium");

    companion object {
        fun fromString(string: String): MembershipPlan
            = entries.firstOrNull { it.string == string } ?: Free
    }
}