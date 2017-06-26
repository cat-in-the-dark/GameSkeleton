package org.catinthedark.server

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Handler(
        val priority: Int = 0
)