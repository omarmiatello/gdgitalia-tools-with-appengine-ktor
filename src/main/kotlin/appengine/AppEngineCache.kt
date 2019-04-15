package com.github.omarmiatello.gdgtools.appengine

import com.google.appengine.api.memcache.Expiration
import com.google.appengine.api.memcache.MemcacheServiceFactory
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.uri
import io.ktor.util.pipeline.PipelineContext

object LocalCache {
    private val cache = mutableMapOf<String, Pair<Long, String>>()

    operator fun get(key: String) =
        cache[key]?.let { (expireInMills, value) -> value.takeIf { System.currentTimeMillis() < expireInMills } }

    operator fun set(key: String, expireInMills: Long = System.currentTimeMillis() + 5 * 60000, value: String) {
        cache[key] = expireInMills to value
    }
}

class AppEngineCache(
    val defaultExpiration: Expiration = Expiration.byDeltaSeconds(3600),
    val useLocalCache: Boolean = false
) {
    val memcache by lazy { MemcacheServiceFactory.getMemcacheService() }

    operator fun get(key: String): String? {
        val localData = if (useLocalCache) LocalCache[key] else null
        return localData ?: (memcache.get(key) as String?)?.also { LocalCache[key] = it }
    }

    operator fun set(key: String, value: String) = memcache.put(key, value, defaultExpiration)
        .also { LocalCache[key] = value }

    inline fun getOrPut(key: String, defaultValue: () -> String) =
        get(key) ?: defaultValue().also { set(key, it) }
}

inline fun PipelineContext<*, ApplicationCall>.cacheUriOr(defaultValue: () -> String) =
    AppEngineCache(useLocalCache = true).getOrPut(call.request.uri, defaultValue)
