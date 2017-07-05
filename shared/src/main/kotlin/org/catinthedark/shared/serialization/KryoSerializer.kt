package org.catinthedark.shared.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output

class KryoSerializer(
        classes: List<Class<*>> = emptyList(),
        private val kryo: Kryo = Kryo()
) : Serializer, Deserializer {
    init {
        classes.forEach { kryo.register(it) }
    }

    override fun invoke(obj: Any): ByteArray {
        return Output().use { output ->
            kryo.writeClassAndObject(output, obj)
            output.flush()
            output
        }.toBytes()
    }

    override fun invoke(data: ByteArray): Any {
        return Input().use { input ->
            input.buffer = data
            kryo.readClassAndObject(input)
        }
    }
}