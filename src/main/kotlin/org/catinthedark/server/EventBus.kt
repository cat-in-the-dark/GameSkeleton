package org.catinthedark.server

import org.catinthedark.server.invokers.DeferrableInvoker
import org.catinthedark.server.invokers.Invoker
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.lang.reflect.Method
import kotlin.reflect.KClass

object EventBus {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val addresses: MutableMap<KClass<*>, MutableSet<Method>> = hashMapOf()

    fun register(packageName: String) {
        Reflections(packageName, MethodAnnotationsScanner())
                .getMethodsAnnotatedWith(Handler::class.java)
                .map { extractInfo(it) }
                .filterNotNull()
                .groupBy { it.klass }
                .map { g ->
                    g.value.sorted().forEach {
                        register(it)
                    }
                }
    }

    fun send(msg: Serializable, holder: Holder<*>, invoker: Invoker) {
        val handlers = addresses[msg::class]
        if (handlers == null) {
            log.warn("There is no handler to handle message of the type ${msg::class.java.canonicalName}")
            return
        }
        handlers.forEach {
            try {
                invoker.invoke { it.invoke(null, msg, holder) }
            } catch (e: Exception) {
                log.error("ERROR! While calling handler '${it.name}' with message $msg and $holder: ${e.cause?.message}", e)
            }
        }
    }

    fun post(msg: Serializable, holder: Holder<*>, ms: Long, invoker: DeferrableInvoker): List<() -> Unit> {
        val handlers = addresses[msg::class]
        if (handlers == null) {
            log.warn("There is no handler to handle message of the type ${msg::class.java.canonicalName}")
            return emptyList()
        }
        return handlers.map {
            try {
                invoker.defer({ it.invoke(null, msg, holder) }, ms)
            } catch (e: Exception) {
                log.error("ERROR! While deffer calling handler '${it.name}' with message $msg and $holder: ${e.cause?.message}", e)
            }

            {}
        }
    }

    private data class Info(
            val klass: KClass<*>,
            val annotation: Handler,
            val method: Method
    ): Comparable<Info> {
        override fun compareTo(other: Info): Int {
            return this.annotation.priority.compareTo(other.annotation.priority)
        }
    }

    private fun extractInfo(method: Method): Info? {
        val clazz = method.parameterTypes[0]?.kotlin ?: return null
        val annotation = method.getAnnotation(Handler::class.java) ?: return null

        return Info(clazz, annotation, method)
    }

    private fun register(info: Info) {
        val methods = addresses.getOrPut(info.klass, { mutableSetOf() })
        methods.add(info.method)
        log.info("REGISTER handler ${info.method.name} to listen to message of ${info.klass}")
    }
}