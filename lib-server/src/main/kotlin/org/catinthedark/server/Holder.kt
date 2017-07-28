package org.catinthedark.server

import java.io.Serializable

data class Holder<out C : Context>(
        val context: C,
        val request: String,
        val extras: MutableMap<Any, Any> = mutableMapOf()
) : Serializable

interface Context : Serializable