package com.github.omarmiatello.gdgtools.appengine

object SlideShareApi {
    private val basePath = "https://www.slideshare.net"
    fun getSlides(userId: String) = Rss2JsonApi.fromRss("$basePath/rss/user/$userId").items
}