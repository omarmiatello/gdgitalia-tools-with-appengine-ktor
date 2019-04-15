package com.github.omarmiatello.gdgtools.appengine


// NOTE: Wait for kotlinx.serialization issues #188
// [FEATURE] support XML serialazation https://github.com/Kotlin/kotlinx.serialization/issues/188

object SpeakerDeckApi {
    private val basePath = "https://speakerdeck.com"
    fun getSlides(userId: String) = Rss2JsonApi.fromRss("$basePath/$userId.atom").items
}