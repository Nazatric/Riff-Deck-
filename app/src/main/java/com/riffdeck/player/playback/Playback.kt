package com.riffdeck.player.playback

object Playback {
    // Custom commands
    const val TOGGLE_SHUFFLE = "com.riffdeck.player.command.shuffle.toggle"
    const val CYCLE_REPEAT = "com.riffdeck.player.command.repeat.cycle"
    const val TOGGLE_FAVORITE = "com.riffdeck.player.command.toggle_favorite"
    const val RESTORE_PLAYBACK = "com.riffdeck.player.command.restore_playback"

    const val SET_UNSHUFFLED_ORDER = "com.riffdeck.player.command.set.unshuffled_order"
    const val SET_STOP_POSITION = "com.riffdeck.player.command.set.stop_position"

    // Custom events
    const val EVENT_MEDIA_CONTENT_CHANGED = "com.riffdeck.player.event.media_content_changed"
    const val EVENT_FAVORITE_CONTENT_CHANGED = "com.riffdeck.player.event.favorite_content_changed"
    const val EVENT_PLAYBACK_RESTORED = "com.riffdeck.player.event.playback_restored"
    const val EVENT_PLAYBACK_STARTED = "com.riffdeck.player.event.playback_started"
}