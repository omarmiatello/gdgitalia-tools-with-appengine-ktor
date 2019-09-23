package com.github.omarmiatello.gdgtools.config

object ExampleConfig : AppConfig() {
    init {
        meetup {
            oauthClientId = ""
            oauthClientSecret = ""
            oauthClientRefreshToken = ""
            oauthRedirect = ""
        }
        telegram {
            apiKey = ""

            // chatId: Unique identifier for the target chat or username of the target channel (in the format @channelusername)

            chatId_all_events = "@gdgeventi"

            tag_chatId_map = mapOf(
                "google-io" to "@community_gdgitalia",
                "devfest" to "@community_gdgitalia",
                "aperitech" to "@community_gdgitalia",
                "hash-code" to "@community_gdgitalia",
                "team-building" to "@community_gdgitalia",
                "codelab" to "@community_gdgitalia",
                "viewing-party" to "@community_gdgitalia",
                "study-jam" to "@community_gdgitalia",
                "women-techmakers" to "@community_gdgitalia",
                "community" to "@community_gdgitalia",
                "android" to "@app_gdgitalia",
                "assistant" to "@ai_gdgitalia",
                "kotlin" to "@app_gdgitalia",
                "firebase" to "@app_gdgitalia",
                "flutter" to "@app_gdgitalia",
                "web" to "@web_gdgitalia",
                "progressive-web-app" to "@web_gdgitalia",
                "google-cloud" to "@web_gdgitalia",
                "app-engine" to "@web_gdgitalia",
                "cloud-next" to "@web_gdgitalia",
                "container" to "@web_gdgitalia",
                "chrome" to "@web_gdgitalia",
                "machine-learning" to "@ai_gdgitalia",
                "augmented-reality" to "@ai_gdgitalia",
                "iot" to "@ai_gdgitalia",
                "javascript" to "@web_gdgitalia"
            )

            chatId_name_map = mapOf(
                "@community_gdgitalia" to "Community",
                "@mobile_gdgitalia" to "Mobile app",
                "@ai_gdgitalia" to "AI + Assistant",
                "@web_gdgitalia" to "Cloud + Web"
            )
        }
        rss2json {
            apiKey = ""
        }
    }
}