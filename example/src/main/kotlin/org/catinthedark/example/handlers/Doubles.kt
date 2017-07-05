package org.catinthedark.example.handlers

import org.catinthedark.shared.Handler
import org.catinthedark.server.Holder

@Handler
fun onDouble(msg: Double, h: Holder<GameContext>) {
    h.context.data += 1
    println("Double '$msg'. Holder: $h")
}