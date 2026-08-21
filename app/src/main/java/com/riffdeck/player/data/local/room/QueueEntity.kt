package com.riffdeck.player.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class QueueEntity(
    @PrimaryKey
    val id: String,
    val order: Int
)