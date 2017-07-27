package org.catinthedark.shared.serialization

import com.esotericsoftware.kryo.Kryo
import org.reflections.Reflections
import java.io.Serializable
import kotlin.reflect.KClass

object KryoCustomizer {
    fun register(kryo: Kryo, vararg klasses: KClass<*>) {
        kryo.apply {
            klasses.forEach {
                kryo.register(it.java, ImmutableClassSerializer(it))
            }
        }
    }

    fun register(kryo: Kryo, klasses: Iterable<KClass<*>>) {
        kryo.apply {
            klasses.forEach {
                kryo.register(it.java, ImmutableClassSerializer(it))
            }
        }
    }

    fun buildAndRegister(packageName: String): Kryo {
        val klasses = Reflections(packageName).getSubTypesOf(Serializable::class.java)
        val kryo = Kryo()
        register(kryo, klasses.map { it.kotlin })
        return kryo
    }
}