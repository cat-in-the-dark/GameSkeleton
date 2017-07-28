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

typealias PreHandler = (target: Any?, message: Any, ctx: List<Any>) -> Pair<Any, List<Any>>

/**
 * Hidden holder of all the inner data that shared between {@see EventBus} and @{@see BusRegister}
 */
private object BusHolder {
    /**
     * The key is a Class of a message.
     * The value is a Set of receivers subscribed on this key Class.
     */
    val addresses: MutableMap<KClass<*>, MutableSet<Info>> = hashMapOf()
    val preHandlers: MutableMap<String, PreHandler> = hashMapOf()
    val lock: ReadWriteLock = ReentrantReadWriteLock()

    data class Info(
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

/**
 * Use this object to send, postpone-send events to EventBus
 * {@see BusRegister} for registration event handlers
 */
object EventBus {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val defaultPreHandler: PreHandler = {_,msg,ctx -> Pair(msg, ctx) }

    /**
     * Send the [message] to some receivers which are defined in the [BusHolder.addresses].
     * Some extras could be passed as [ctx] - a list of objects.
     * The addressing system relies on the [message] type.
     * The [message] will be handled by some handlers with a strategy provided by the [invoker].
     */
    fun send(from: String = "Events#send", invoker: Invoker, message: Any, vararg ctx: Any) {
        BusHolder.lock.readLock().withLock {
            val handlers = BusHolder.addresses[message::class]
            if (handlers == null) {
                log.warn("$from: There is no handler to handle message of the type ${message::class.java.canonicalName}")
                return
            }
            handlers.forEach {
                try {
                    invoker.invoke {
                        var newCtx: List<Any> = emptyList()
                        try {
                            val preHandler = BusHolder.preHandlers.getOrDefault(it.annotation.preHandlerPath, defaultPreHandler)
                            val (msg, c) = preHandler(it.target, message, ctx.asList())
                            newCtx = c
                            it.method.invoke(it.target, msg, *newCtx.toTypedArray())
                        } catch (e: Exception) {
                            val expectedTypes = it.method.parameterTypes.map { it.canonicalName }
                            val gotTypes = newCtx.map { it.javaClass.canonicalName }
                            log.error("$from: Can't invoke method ${it.method} for target ${it.target} with $message and $newCtx.\nExpected types:\t$expectedTypes.\nGot types:\t$gotTypes", e)
                        }
                    }
                } catch (e: Exception) {
                    log.error("$from: Can't invoke method '${it.method.name}' for target ${it.target} with message $message and $ctx: ${e.cause?.message}", e)
                }
            }
        }
    }

    /**
     * The same as [send] method, but send the [message] in the future
     * with the [timeout] in time units, that supports by the [invoker].
     */
    fun post(from: String = "Events#post", invoker: DeferrableInvoker, timeout: Long, message: Any, vararg ctx: Any): List<() -> Unit> {
        BusHolder.lock.readLock().withLock {
            val handlers = BusHolder.addresses[message::class]
            if (handlers == null) {
                log.warn("$from: There is no handler to handle message of the type ${message::class.java.canonicalName}")
                return emptyList()
            }
            return handlers.map {
                try {
                    invoker.defer({
                        try {
                            it.method.invoke(it.target, message, *ctx)
                        } catch (e: Exception) {
                            log.error("$from: Can't invoke method ${it.method} for target ${it.target} with $message and $ctx", e)
                        }
                    }, timeout)
                } catch (e: Exception) {
                    log.error("$from: Can't invoke method '${it.method.name}' for target ${it.target} with message $message and $ctx: ${e.cause?.message}", e)
                }

                {}
            }
        }
    }
}

/**
 * Use this object to register handlers in the {@see EventBus}
 * You can register all the classes in the package. It's useful for regestering stateless functions.
 * You can register an instance of the class. Creating new instance create a new listener. Useful for stateful handlers.
 */
object BusRegister {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun registerPreHandler(preHandlerPath: String, preHandler: PreHandler) {
        BusHolder.preHandlers.put(preHandlerPath, preHandler)
    }

    fun unRegisterPreHandler(preHandlerPath: String) {
        BusHolder.preHandlers.remove(preHandlerPath)
    }

    /**
     * Look through the static methods defined in the package
     * and register them.
     */
    fun register(packageName: String) {
        BusHolder.lock.writeLock().withLock {
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
        BusHolder.lock.writeLock().withLock {
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
        BusHolder.lock.writeLock().withLock {
            BusHolder.addresses.forEach { _, info ->
                info.removeIf {
                    it.target == target
                }
            }
            BusHolder.addresses.filter {
                it.value.isEmpty()
            }.forEach { key, _ ->
                BusHolder.addresses.remove(key)
            }
        }
    }

    private fun extractInfo(method: Method, target: Any?): BusHolder.Info? {
        val clazz = method.parameterTypes[0]?.kotlin ?: return null
        val annotation = method.getAnnotation(Handler::class.java) ?: return null

        return BusHolder.Info(clazz, annotation, method, target)
    }

    private fun register(info: BusHolder.Info) {
        val methods = BusHolder.addresses.getOrPut(info.klass, { mutableSetOf() })
        methods.add(info)
        log.info("REGISTER handler ${info.method.name} to listen to message of ${info.klass}")
    }
}