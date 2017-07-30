package org.catinthedark.example.shared.messages

import org.catinthedark.shared.serialization.Message

@Message
data class OnGameStart(
    val data: String
)
