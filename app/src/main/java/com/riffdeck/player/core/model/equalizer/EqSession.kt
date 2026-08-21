package com.riffdeck.player.core.model.equalizer

data class EqSession(
    val type: SessionType,
    val id: Int,
    val active: Boolean
) {
    enum class SessionType {
        Internal, External
    }
}