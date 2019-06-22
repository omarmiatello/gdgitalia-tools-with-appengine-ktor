package com.github.omarmiatello.gdgtools.utils

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.gson.Gson
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.typeTokenOf


@UnstableDefault
val json = Json(JsonConfiguration.Default.copy(prettyPrint = true))

@UnstableDefault
val jsonNonStrict = Json(JsonConfiguration.Default.copy(strictMode = false))


@UnstableDefault
fun <T> T.toJsonContent(serializer: SerializationStrategy<T>) =
    ByteArrayContent("application/json", json.stringify(serializer, this).toByteArray())

@UnstableDefault
fun <T> T.toJsonPretty(serializer: SerializationStrategy<T>): String = json.stringify(serializer, this)

@UnstableDefault
inline fun <reified T> HttpResponse.parse(serializer: DeserializationStrategy<T>): T? {
    if (isSuccessStatusCode) {
        return parseAsString()
            .takeIf { it != "null" }
            ?.let { jsonNonStrict.parse(serializer, it) }
    } else {
        throw HttpResponseException(this)
    }
}

@UnstableDefault
inline fun <reified T> HttpResponse.parseNotNull(serializer: DeserializationStrategy<T>): T {
    if (isSuccessStatusCode) {
        return jsonNonStrict.parse(serializer, parseAsString())
    } else {
        throw HttpResponseException(this)
    }
}






// https://github.com/Kotlin/kotlinx.serialization/issues/58
// WORKAROUND use Gson instead of kotlinx.serialization
inline fun <reified T> HttpResponse.parseForTelegram(): T {
    if (isSuccessStatusCode) {
        val response = parseAsString()
        println(response)
        return Gson().fromJson(response, typeTokenOf<T>())
    } else {
        throw HttpResponseException(this)
    }
}

// https://github.com/Kotlin/kotlinx.serialization/issues/58
// WORKAROUND use Gson instead of kotlinx.serialization
fun <T> T.toJsonContentForTelegram() =
    ByteArrayContent("application/json", Gson().toJson(this).toByteArray())