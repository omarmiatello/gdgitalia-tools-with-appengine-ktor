package com.github.omarmiatello.gdgtools.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DeferredLazy<T>(private val block: suspend () -> T) {
    private var deferred: Deferred<T>? = null

    suspend fun await(): T = coroutineScope {
        if (deferred == null) {
            deferred = async { block() }
        }

        deferred!!.await()
    }
}