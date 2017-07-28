package org.catinthedark.shared.event_bus

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Handler(
        val priority: Int = 0,
        val preHandlerPath: String = ""
)