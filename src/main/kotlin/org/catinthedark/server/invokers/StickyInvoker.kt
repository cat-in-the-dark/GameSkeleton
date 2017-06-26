package org.catinthedark.server.invokers

class StickyInvoker: Invoker {
    override fun shutdown() {
        // Nothing to do
    }

    override fun invoke(func: () -> Unit) {
        func()
    }
}