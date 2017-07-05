package org.catinthedark.shared

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Handler(
        val priority: Int = 0
)