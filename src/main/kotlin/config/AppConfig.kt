package com.github.omarmiatello.gdgtools.config

@DslMarker
annotation class AppConfigMarker

@AppConfigMarker
abstract class AppConfig {
    val meetup = MEETUP()
    val telegram = TELEGRAM()
    val rss2json = RSS2JSON()

    fun meetup(conf: MEETUP.() -> Unit) {
        meetup.conf()
    }

    fun telegram(conf: TELEGRAM.() -> Unit) {
        telegram.conf()
    }

    fun rss2json(conf: RSS2JSON.() -> Unit) {
        rss2json.conf()
    }

    companion object {
        fun getDefault(): AppConfig = MyConfig
    }
}

@AppConfigMarker
class MEETUP {
    var oauthClientId: String = ""
    var oauthClientSecret: String = ""
    var oauthClientRefreshToken: String = ""
    var oauthRedirect: String = ""
}

@AppConfigMarker
class TELEGRAM {
    var apiKey: String = ""

    // chatId: Unique identifier for the target chat or username of the target channel (in the format @channelusername)

    var chatId_all_events: String = ""

    var tag_chatId_map: Map<String, String> = emptyMap()

    var chatId_name_map: Map<String, String> = emptyMap()
}

@AppConfigMarker
class RSS2JSON {
    var apiKey: String = ""
}
