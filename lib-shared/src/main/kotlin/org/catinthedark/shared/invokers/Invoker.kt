package org.catinthedark.shared.invokers

interface Invoker {
    fun invoke(func: () -> Unit)
    fun shutdown()
}

interface DeferrableInvoker : Invoker {
    fun defer(func: () -> Unit, timeout: Long): () -> Unit
    fun periodic(func: () -> Unit, timeout: Long): () -> Unit
}