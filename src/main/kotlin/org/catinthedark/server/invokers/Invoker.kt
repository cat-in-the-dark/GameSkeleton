package org.catinthedark.server.invokers

interface Invoker {
    fun invoke(func: () -> Unit)
    fun shutdown()
}

interface DeferrableInvoker : Invoker {
    fun defer(func: () -> Unit, ms: Long): () -> Unit
    fun periodic(func: () -> Unit, ms: Long): () -> Unit
}