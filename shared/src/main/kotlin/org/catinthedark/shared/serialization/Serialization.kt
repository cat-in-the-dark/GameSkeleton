package org.catinthedark.shared.serialization

interface Serializer {
    operator fun invoke(obj: Any): ByteArray
}

interface Deserializer {
    operator fun invoke(data: ByteArray): Any
}