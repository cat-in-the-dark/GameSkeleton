package org.catinthedark.server.handlers

import org.catinthedark.server.Handler
import org.catinthedark.server.Holder

@Handler(priority = 0)
fun onMovement(msg: String, h: Holder<GameContext>) {
    h.context.data += 1
    Thread.sleep(1000)
    println("Move '$msg'. Holder: $h")
}

@Handler(priority = 1)
fun onMovement2(msg: String, h: Holder<GameContext>) {
    h.context.data += 1
    println("Move2 '$msg'. Holder: $h")
}