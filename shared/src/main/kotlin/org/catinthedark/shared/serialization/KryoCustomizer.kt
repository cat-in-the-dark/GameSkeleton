package org.catinthedark.shared.serialization

import com.esotericsoftware.kryo.Kryo
import kotlin.reflect.KClass

object KryoCustomizer {
    fun register(kryo: Kryo, vararg klasses: KClass<*>) {
        kryo.apply {
            klasses.forEach {
                kryo.register(it.java, ImmutableClassSerializer(it))
            }
        }
    }
}