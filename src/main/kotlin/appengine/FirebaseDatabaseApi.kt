package com.github.omarmiatello.gdgtools.appengine

import com.github.omarmiatello.gdgtools.utils.parse
import com.github.omarmiatello.gdgtools.utils.toJsonContent
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import kotlinx.serialization.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


@Serializable
class PutResponse(val name: String)

// The following methods are to illustrate making various calls to
// Firebase from App Engine Standard
abstract class FirebaseDatabaseApi {
    abstract val basePath: String

    private val httpTransport = UrlFetchTransport.getDefaultInstance()

    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory(
        GoogleCredential.getApplicationDefault().createScoped(
            listOf(
                "https://www.googleapis.com/auth/firebase.database",
                "https://www.googleapis.com/auth/userinfo.email"
            )
        )
    )

    fun get(path: String) = requestFactory()
        .buildGetRequest(GenericUrl(path))
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun <T> post(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPostRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .parse(PutResponse.serializer())?.name

    private fun <T> put(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPutRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun <T> patch(path: String, obj: T, serializer: SerializationStrategy<T>) = requestFactory()
        .buildPatchRequest(
            GenericUrl(path),
            obj.toJsonContent(serializer)
        )
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }

    private fun delete(path: String): HttpResponse = requestFactory()
        .buildDeleteRequest(GenericUrl(path))
        .execute()
        .apply { if (!isSuccessStatusCode) throw HttpResponseException(this) }


    inline operator fun <reified T> get(path: String, resp: KSerializer<T>) =
        get("$basePath$path.json").parse(resp)

    operator fun <T> set(path: String, serializer: KSerializer<T>, obj: T) =
        put("$basePath$path.json", obj, serializer)

    protected fun <T> addItem(path: String, obj: T, serializer: KSerializer<T>) =
        post("$basePath$path.json", obj, serializer)

    protected fun <T> update(path: String, map: Map<String, T>, serializer: KSerializer<T>) =
        patch("$basePath$path.json", map, (String.serializer() to serializer).map)

    protected fun deletePath(path: String) = delete("$basePath$path.json")
}

inline fun <reified T> fireProperty(key: String, serializer: KSerializer<T>) =
    object : ReadWriteProperty<FirebaseDatabaseApi, T?> {
        override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): T? {
            return thisRef[key, serializer]
        }

        override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: T?) {
            thisRef[key, serializer] = value ?:
                    throw IllegalArgumentException("Use deletePath() insted of set `null` in path: $key")
        }
    }

inline fun <reified T> fireList(key: String, serializer: KSerializer<Collection<T>>) =
    object : ReadWriteProperty<FirebaseDatabaseApi, Collection<T>> {
        override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): Collection<T> {
            return thisRef[key, serializer].orEmpty()
        }

        override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: Collection<T>) {
            thisRef[key, serializer] = value
        }
    }

inline fun <reified T> fireMap(key: String, serializer: KSerializer<Map<String, T>>) =
    object : ReadWriteProperty<FirebaseDatabaseApi, Map<String, T>> {
        override fun getValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>): Map<String, T> {
            return thisRef[key, serializer].orEmpty()
        }

        override fun setValue(thisRef: FirebaseDatabaseApi, property: KProperty<*>, value: Map<String, T>) {
            thisRef[key, serializer] = value
        }
    }