package com.github.omarmiatello.gdgtools.appengine

import com.github.omarmiatello.gdgtools.config.AppConfig
import com.github.omarmiatello.gdgtools.utils.parseNotNull
import com.github.omarmiatello.gdgtools.utils.toJsonContentForTelegram
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

object SlackApi {
    private val config = AppConfig.getDefault().slack
    private val basePath = config.webhook
    private val httpTransport = UrlFetchTransport.getDefaultInstance()
    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory()

    fun sendMessage(
        text: String
    ) = requestFactory()
        .buildPostRequest(
            GenericUrl(basePath),
            MessageRequest(
                text = text
            ).toJsonContentForTelegram()
        )
        .execute()

    @Serializable
    data class MessageRequest(
        val text: String
    )
}

fun String.htmlToSlackMarkdown() =
    replace("<br.*?>".toRegex(), "\n")
        .replace("</p>", "\n")
        .replace("<b>", "*")
        .replace("</b>", "*")
        .replace("&gt;", ">")
        .replace("&lt;", "<")
        .replace("&amp;", "&")
        .replace("&quot;", "'")
        .replace("<h\\d>".toRegex(), "*")
        .replace("</h\\d>".toRegex(), "*\n")
        .replace("<.*?>".toRegex(), "")
        .lines()
        .map { it.trim() }
        .joinToString("\n")