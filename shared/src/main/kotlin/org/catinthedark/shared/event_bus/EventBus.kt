package org.catinthedark.shared.event_bus

import org.catinthedark.shared.invokers.DeferrableInvoker
import org.catinthedark.shared.invokers.Invoker
import org.reflections.ReflectionUtils.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

object Events {
    private val addresses: MutableMap<KClass<*>, MutableSet<Info>> = hashMapOf()
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    object Bus {
        private val log = LoggerFactory.getLogger(this::class.java)

        fun send(invoker: Invoker, msg: Any, vararg ctx: Any) {
            lock.readLock().withLock {
                val handlers = addresses[msg::class]
                if (handlers == null) {
                    log.warn("There is no handler to handle message of the type ${msg::class.java.canonicalName}")
                    return
                }
                handlers.forEach {
                    try {
                        invoker.invoke {
                            try {
                                it.method.invoke(it.target, msg, *ctx)
                            } catch (e: Exception) {
                                log.error("Can't invoke method ${it.method} for target ${it.target} with $msg and $ctx", e)
                            }
                        }
                    } catch (e: Exception) {
                        log.error("Can't invoke method '${it.method.name}' for target ${it.target} with message $msg and $ctx: ${e.cause?.message}", e)
                    }
                }
            }
        }

        fun post(invoker: DeferrableInvoker, ms: Long, msg: Any, vararg ctx: Any): List<() -> Unit> {
            lock.readLock().withLock {
                val handlers = addresses[msg::class]
                if (handlers == null) {
                    log.warn("There is no handler to handle message of the type ${msg::class.java.canonicalName}")
                    return emptyList()
                }
                return handlers.map {
                    try {
                        invoker.defer({
                            try {
                                it.method.invoke(it.target, msg, *ctx)
                            } catch (e: Exception) {
                                log.error("Can't invoke method ${it.method} for target ${it.target} with $msg and $ctx", e)
                            }
                        }, ms)
                    } catch (e: Exception) {
                        log.error("Can't invoke method '${it.method.name}' for target ${it.target} with message $msg and $ctx: ${e.cause?.message}", e)
                    }

                    {}
                }
            }
        }
    }

    object Registrator {
        private val log = LoggerFactory.getLogger(this::class.java)

        /**
         * Look through the static methods defined in the package
         * and register them.
         */
        fun register(packageName: String) {
            lock.writeLock().withLock {
                Reflections(packageName, MethodAnnotationsScanner())
                        .getMethodsAnnotatedWith(Handler::class.java)
                        .filter {
                            Modifier.isStatic(it.modifiers)
                        }
                        .map { extractInfo(it, null) }
                        .filterNotNull()
                        .groupBy { it.klass }
                        .map { g ->
                            g.value.sorted().forEach {
                                register(it)
                            }
                        }
            }
        }

        /**
         * Add all annotated methods in this particular class instance
         */
        fun register(target: Any) {
            lock.writeLock().withLock {
                getAllMethods(
                        target::class.java,
                        withModifier(Modifier.PUBLIC),
                        withAnnotation(Handler::class.java)
                ).map {
                    extractInfo(it, target)
                }.filterNotNull().forEach {
                    register(it)
                }
            }
        }

        /**
         * Remove all subscribers with this target from the message bus
         */
        fun unregister(target: Any?) {
            lock.writeLock().withLock {
                addresses.forEach { _, info ->
                    info.removeIf {
                        it.target == target
                    }
                }
                addresses.filter {
                    it.value.isEmpty()
                }.forEach { key, _ ->
                    addresses.remove(key)
                }
            }
        }

        private fun extractInfo(method: Method, target: Any?): Info? {
            val clazz = method.parameterTypes[0]?.kotlin ?: return null
            val annotation = method.getAnnotation(Handler::class.java) ?: return null

            return Info(clazz, annotation, method, target)
        }

        private fun register(info: Info) {
            val methods = addresses.getOrPut(info.klass, { mutableSetOf() })
            methods.add(info)
            log.info("REGISTER handler ${info.method.name} to listen to message of ${info.klass}")
        }
    }

    private data class Info(
            val klass: KClass<*>,
            val annotation: Handler,
            val method: Method,
            val target: Any? = null
    ) : Comparable<Info> {
        override fun compareTo(other: Info): Int {
            return this.annotation.priority.compareTo(other.annotation.priority)
        }
    }
}