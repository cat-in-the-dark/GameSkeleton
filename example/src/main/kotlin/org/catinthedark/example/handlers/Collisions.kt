package org.catinthedark.example.handlers

import org.catinthedark.shared.Handler
import org.catinthedark.server.Holder

@Handler
fun onCollision(msg: Int, h: Holder<GameContext>) {
    h.context.data += 1
    println("Collide '$msg'. Holder: $h")
}