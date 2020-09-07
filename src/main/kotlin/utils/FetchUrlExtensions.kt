package com.github.omarmiatello.gdgtools.utils

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.gson.Gson
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json


val json = Json { prettyPrint = true }

val jsonNonStrict = Json { ignoreUnknownKeys = true }


fun <T> T.toJsonContent(serializer: SerializationStrategy<T>) =
    ByteArrayContent("application/json", json.encodeToString(serializer, this).toByteArray())

fun <T> T.toJsonPretty(serializer: SerializationStrategy<T>): String = json.encodeToString(serializer, this)

inline fun <reified T> HttpResponse.parse(serializer: DeserializationStrategy<T>): T? {
    if (isSuccessStatusCode) {
        return parseAsString()
            .takeIf { it != "null" }
            ?.let { jsonNonStrict.decodeFromString(serializer, it) }
    } else {
        throw HttpResponseException(this)
    }
}

inline fun <reified T> HttpResponse.parseNotNull(serializer: DeserializationStrategy<T>): T {
    if (isSuccessStatusCode) {
        return jsonNonStrict.decodeFromString(serializer, parseAsString())
    } else {
        throw HttpResponseException(this)
    }
}

// https://github.com/Kotlin/kotlinx.serialization/issues/58
// WORKAROUND use Gson instead of kotlinx.serialization
fun <T> T.toJsonContentForTelegram() =
    ByteArrayContent("application/json", Gson().toJson(this).toByteArray())