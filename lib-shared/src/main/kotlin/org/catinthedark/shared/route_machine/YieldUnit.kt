package org.catinthedark.shared.route_machine

interface YieldUnit<in U, out T> {
    fun onActivate(data: U)
    fun run(delta: Float): T?
    fun onExit()
}