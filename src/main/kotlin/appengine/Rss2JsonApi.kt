package com.github.gdgitalia.tools.appengine

import com.github.gdgitalia.tools.config.AppConfig
import com.github.gdgitalia.tools.data.SlideDao
import com.github.gdgitalia.tools.data.findHashtags
import com.github.gdgitalia.tools.utils.parseNotNull
import com.github.gdgitalia.tools.utils.toSlug
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import io.ktor.http.encodeURLQueryComponent
import kotlinx.serialization.Serializable

object Rss2JsonApi {
    private val config = AppConfig.getDefault().rss2json
    private val basePath = "https://api.rss2json.com"
    private val httpTransport = UrlFetchTransport.getDefaultInstance()
    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory()

    val apiKeyParam = if (config.apiKey.isNotEmpty()) "api_key=${config.apiKey}" else ""

    fun fromRss(url: String) = requestFactory()
        .buildGetRequest(GenericUrl("$basePath/v1/api.json?$apiKeyParam&rss_url=${url.encodeURLQueryComponent()}"))
        .execute()
        .parseNotNull(RssResponse.serializer())

    @Serializable
    data class RssResponse (
        //  val status: String,
        val feed: Feed,
        val items: List<Item>
    )

    @Serializable
    data class Feed (
        val url: String,
        val title: String,
        val link: String
        // val author: String,
        // val description: String,
        // val image: String
    )

    @Serializable
    data class Item (
        val title: String,
        val pubDate: String,
        val link: String,
        val guid: String,
        val author: String,
        val thumbnail: String,
        // val description: String,
        val content: String
    ) {
        fun toSlideDao(speakerSlug: String) = SlideDao(
            slug = title.toSlug(),
            name = title,
            description = content,
            pubDate = pubDate,
            link = link,
            speakerSlug = speakerSlug,
            author = author,
            image = thumbnail,
            tags = (title + content).findHashtags().joinToString(" ")
        )
    }
}

// https://feed2json.org/convert?url=https://speakerdeck.com/jacklt.atom
